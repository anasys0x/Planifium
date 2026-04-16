package com.diro.ift2255.repository;

import com.diro.ift2255.model.ConflitHoraire;
import com.diro.ift2255.model.EnsembleCours;
import com.diro.ift2255.model.Horaire;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Repository pour la détection et la gestion des conflits d'horaire.
 * Fournit des algorithmes de détection de chevauchements entre créneaux.
 * 
 * <p>Cette classe implémente la logique de détection des conflits en comparant
 * toutes les paires d'horaires possibles. Elle utilise également un cache
 * pour optimiser les performances des requêtes répétées.</p>
 * 
 * @author Équipe 10
 * @version 3.0
 * @since 2025-12-27
 * @see ConflitHoraire
 * @see Horaire
 * @see com.diro.ift2255.service.ConflitHoraireService
 */
public class ConflitHoraireRepository {
    
    /**
     * Cache des conflits détectés par ID d'ensemble.
     * Clé: ID de l'ensemble, Valeur: Liste des conflits.
     */
    private final Map<String, List<ConflitHoraire>> conflitsCache;
    
    /**
     * Constructeur par défaut.
     * Initialise le cache vide.
     */
    public ConflitHoraireRepository() {
        this.conflitsCache = new ConcurrentHashMap<>();
    }
    
    /**
     * Détecte tous les conflits dans une liste d'horaires.
     * Compare chaque paire d'horaires de cours différents.
     * 
     * @param horaires La liste des horaires à analyser
     * @return Liste des conflits détectés (vide si aucun conflit)
     */
    public List<ConflitHoraire> detecterConflits(List<Horaire> horaires) {
        List<ConflitHoraire> conflits = new ArrayList<>();
        
        if (horaires == null || horaires.size() < 2) {
            return conflits;
        }
        
        for (int i = 0; i < horaires.size(); i++) {
            for (int j = i + 1; j < horaires.size(); j++) {
                Horaire h1 = horaires.get(i);
                Horaire h2 = horaires.get(j);
                
                if (h1.getCourseId() != null && h1.getCourseId().equals(h2.getCourseId())) {
                    continue;
                }
                
                if (h1.chevauche(h2)) {
                    conflits.add(new ConflitHoraire(h1, h2));
                }
            }
        }
        
        return conflits;
    }
    
    /**
     * Détecte les conflits pour un ensemble de cours.
     * Utilise le cache si disponible.
     * 
     * @param ensemble L'ensemble de cours à analyser
     * @return Liste des conflits détectés
     */
    public List<ConflitHoraire> detecterConflitsPourEnsemble(EnsembleCours ensemble) {
        if (ensemble == null) {
            return new ArrayList<>();
        }
        
        String cacheKey = ensemble.getId();
        if (cacheKey != null && conflitsCache.containsKey(cacheKey)) {
            return new ArrayList<>(conflitsCache.get(cacheKey));
        }
        
        List<Horaire> tousHoraires = new ArrayList<>();
        Map<String, List<Horaire>> horairesMap = ensemble.getHoraires();
        
        if (horairesMap != null) {
            for (List<Horaire> horairesList : horairesMap.values()) {
                tousHoraires.addAll(horairesList);
            }
        }
        
        List<ConflitHoraire> conflits = detecterConflits(tousHoraires);
        
        if (cacheKey != null) {
            conflitsCache.put(cacheKey, new ArrayList<>(conflits));
        }
        
        return conflits;
    }
    
    /**
     * Vérifie si l'ajout d'un horaire créerait des conflits.
     * 
     * @param horairesExistants Les horaires déjà présents
     * @param nouvelHoraire Le nouvel horaire à tester
     * @return Liste des conflits potentiels (vide si aucun)
     */
    public List<ConflitHoraire> verifierAjoutHoraire(List<Horaire> horairesExistants, Horaire nouvelHoraire) {
        List<ConflitHoraire> conflits = new ArrayList<>();
        
        if (horairesExistants == null || nouvelHoraire == null) {
            return conflits;
        }
        
        for (Horaire existant : horairesExistants) {
            if (existant.chevauche(nouvelHoraire)) {
                conflits.add(new ConflitHoraire(existant, nouvelHoraire));
            }
        }
        
        return conflits;
    }
    
    /**
     * Groupe les conflits par jour de la semaine.
     * 
     * @param conflits La liste des conflits à grouper
     * @return Map associant chaque jour à sa liste de conflits
     */
    public Map<String, List<ConflitHoraire>> grouperParJour(List<ConflitHoraire> conflits) {
        if (conflits == null) {
            return new HashMap<>();
        }
        
        return conflits.stream()
                .filter(c -> c.getHoraire1() != null && c.getHoraire1().getJour() != null)
                .collect(Collectors.groupingBy(c -> c.getHoraire1().getJourString()));
    }
    
    /**
     * Compte les conflits par type (TOTAL vs PARTIEL).
     * 
     * @param conflits La liste des conflits à compter
     * @return Map associant chaque type à son nombre d'occurrences
     */
    public Map<ConflitHoraire.TypeConflit, Long> compterParType(List<ConflitHoraire> conflits) {
        if (conflits == null) {
            return new EnumMap<>(ConflitHoraire.TypeConflit.class);
        }
        
        return conflits.stream()
                .filter(c -> c.getType() != null)
                .collect(Collectors.groupingBy(ConflitHoraire::getType, Collectors.counting()));
    }
    
    /**
     * Calcule le temps total de chevauchement de tous les conflits.
     * 
     * @param conflits La liste des conflits
     * @return Le temps total en minutes
     */
    public long calculerTempsTotal(List<ConflitHoraire> conflits) {
        if (conflits == null) {
            return 0;
        }
        
        return conflits.stream()
                .mapToLong(ConflitHoraire::getDureeChevauchementsMinutes)
                .sum();
    }
    
    /**
     * Invalide le cache pour un ensemble spécifique.
     * Doit être appelé après toute modification de l'ensemble.
     * 
     * @param ensembleId L'ID de l'ensemble dont le cache doit être invalidé
     */
    public void invaliderCache(String ensembleId) {
        if (ensembleId != null) {
            conflitsCache.remove(ensembleId);
        }
    }
    
    /**
     * Vide entièrement le cache des conflits.
     */
    public void viderCache() {
        conflitsCache.clear();
    }
}
