package com.diro.ift2255.service;

import com.diro.ift2255.model.ConflitHoraire;
import com.diro.ift2255.model.EnsembleCours;
import com.diro.ift2255.model.Horaire;
import com.diro.ift2255.repository.ConflitHoraireRepository;
import com.diro.ift2255.repository.EnsembleCoursRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service pour la détection des conflits d'horaire (CU#11 - Bonus 2%).
 * Implémente la logique métier pour détecter et analyser les conflits.
 * 
 * <p>Ce service analyse les horaires des cours d'un ensemble pour identifier
 * les chevauchements temporels. Il fournit également des suggestions pour
 * résoudre les conflits détectés.</p>
 * 
 * <h2>Fonctionnalités principales :</h2>
 * <ul>
 *   <li>Détection des conflits totaux et partiels</li>
 *   <li>Génération de résumés statistiques</li>
 *   <li>Groupement des conflits par jour</li>
 *   <li>Suggestions de résolution</li>
 *   <li>Vérification préalable avant ajout de cours</li>
 * </ul>
 * 
 * @author Équipe 10
 * @version 3.0
 * @since 2025-12-27
 * @see ConflitHoraire
 * @see ConflitHoraireRepository
 */
public class ConflitHoraireService {
    
    /**
     * Repository pour les opérations sur les conflits.
     */
    private final ConflitHoraireRepository conflitRepository;
    
    /**
     * Repository pour accéder aux ensembles.
     */
    private final EnsembleCoursRepository ensembleRepository;
    
    /**
     * Constructeur avec injection de dépendances.
     * 
     * @param conflitRepository Le repository des conflits
     * @param ensembleRepository Le repository des ensembles
     */
    public ConflitHoraireService(ConflitHoraireRepository conflitRepository, 
                                  EnsembleCoursRepository ensembleRepository) {
        this.conflitRepository = conflitRepository;
        this.ensembleRepository = ensembleRepository;
    }
    
    /**
     * Détecte tous les conflits d'horaire dans un ensemble.
     * 
     * @param ensembleId L'ID de l'ensemble à analyser
     * @return Liste des conflits détectés (vide si aucun conflit)
     * @throws IllegalArgumentException si l'ensemble n'existe pas
     */
    public List<ConflitHoraire> detecterConflits(String ensembleId) {
        EnsembleCours ensemble = ensembleRepository.findById(ensembleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ensemble non trouvé avec l'ID: " + ensembleId));
        
        return conflitRepository.detecterConflitsPourEnsemble(ensemble);
    }
    
    /**
     * Génère un résumé statistique des conflits d'un ensemble.
     * 
     * <p>Le résumé contient :</p>
     * <ul>
     *   <li>nombreConflits : Nombre total de conflits</li>
     *   <li>aDesConflits : Booléen indiquant la présence de conflits</li>
     *   <li>tempsChevauchementsMinutes : Durée totale des chevauchements</li>
     *   <li>conflitsTotaux : Nombre de conflits totaux</li>
     *   <li>conflitsPartiels : Nombre de conflits partiels</li>
     *   <li>coursImpliques : Liste des cours impliqués dans des conflits</li>
     * </ul>
     * 
     * @param ensembleId L'ID de l'ensemble
     * @return Map contenant les statistiques
     * @throws IllegalArgumentException si l'ensemble n'existe pas
     */
    public Map<String, Object> getResumeConflits(String ensembleId) {
        List<ConflitHoraire> conflits = detecterConflits(ensembleId);
        
        Map<String, Object> resume = new HashMap<>();
        resume.put("ensembleId", ensembleId);
        resume.put("nombreConflits", conflits.size());
        resume.put("aDesConflits", !conflits.isEmpty());
        
        long tempsTotal = conflitRepository.calculerTempsTotal(conflits);
        resume.put("tempsChevauchementsMinutes", tempsTotal);
        
        Map<ConflitHoraire.TypeConflit, Long> parType = conflitRepository.compterParType(conflits);
        resume.put("conflitsTotaux", parType.getOrDefault(ConflitHoraire.TypeConflit.TOTAL, 0L));
        resume.put("conflitsPartiels", parType.getOrDefault(ConflitHoraire.TypeConflit.PARTIEL, 0L));
        
        Set<String> coursImpliques = new HashSet<>();
        for (ConflitHoraire conflit : conflits) {
            if (conflit.getCours1Id() != null) coursImpliques.add(conflit.getCours1Id());
            if (conflit.getCours2Id() != null) coursImpliques.add(conflit.getCours2Id());
        }
        resume.put("coursImpliques", new ArrayList<>(coursImpliques));
        resume.put("nombreCoursImpliques", coursImpliques.size());
        
        return resume;
    }
    
    /**
     * Retourne les conflits groupés par jour de la semaine.
     * 
     * @param ensembleId L'ID de l'ensemble
     * @return Map associant chaque jour (en français) à sa liste de conflits
     * @throws IllegalArgumentException si l'ensemble n'existe pas
     */
    public Map<String, List<ConflitHoraire>> getConflitsParJour(String ensembleId) {
        List<ConflitHoraire> conflits = detecterConflits(ensembleId);
        return conflitRepository.grouperParJour(conflits);
    }
    
    /**
     * Génère des suggestions pour résoudre les conflits.
     * 
     * <p>Les suggestions identifient les cours les plus problématiques
     * (ceux impliqués dans le plus grand nombre de conflits) et
     * recommandent de les retirer ou de changer de section.</p>
     * 
     * @param ensembleId L'ID de l'ensemble
     * @return Map contenant les suggestions de résolution
     * @throws IllegalArgumentException si l'ensemble n'existe pas
     */
    public Map<String, Object> genererSuggestionsResolution(String ensembleId) {
        List<ConflitHoraire> conflits = detecterConflits(ensembleId);
        
        Map<String, Object> suggestions = new HashMap<>();
        suggestions.put("ensembleId", ensembleId);
        suggestions.put("nombreConflits", conflits.size());
        
        if (conflits.isEmpty()) {
            suggestions.put("message", "Aucun conflit détecté. Votre horaire est compatible.");
            suggestions.put("actions", new ArrayList<>());
            return suggestions;
        }
        
        Map<String, Integer> coursConflictCount = new HashMap<>();
        for (ConflitHoraire conflit : conflits) {
            String cours1 = conflit.getCours1Id();
            String cours2 = conflit.getCours2Id();
            if (cours1 != null) {
                coursConflictCount.merge(cours1, 1, Integer::sum);
            }
            if (cours2 != null) {
                coursConflictCount.merge(cours2, 1, Integer::sum);
            }
        }
        
        List<Map.Entry<String, Integer>> sorted = coursConflictCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        List<Map<String, Object>> actions = new ArrayList<>();
        for (int i = 0; i < Math.min(3, sorted.size()); i++) {
            Map.Entry<String, Integer> entry = sorted.get(i);
            Map<String, Object> action = new HashMap<>();
            action.put("cours", entry.getKey());
            action.put("nombreConflits", entry.getValue());
            action.put("suggestion", "Envisagez de retirer " + entry.getKey() + 
                    " ou de choisir une autre section");
            actions.add(action);
        }
        
        suggestions.put("actions", actions);
        suggestions.put("message", "Les cours ci-dessus sont les plus problématiques. " +
                "Considérez de les retirer ou de changer de section.");
        
        return suggestions;
    }
    
    /**
     * Vérifie si l'ajout d'un cours créerait des conflits.
     * 
     * @param ensembleId L'ID de l'ensemble
     * @param courseId L'ID du cours à vérifier
     * @param nouvelHoraire Les horaires du nouveau cours
     * @return Map contenant le résultat de la vérification avec :
     *         - peutAjouter : booléen indiquant si l'ajout est possible
     *         - nombreConflitsPotentiels : nombre de conflits créés
     *         - conflits : liste des conflits potentiels
     * @throws IllegalArgumentException si l'ensemble n'existe pas
     */
    public Map<String, Object> verifierAjoutCours(String ensembleId, String courseId, 
                                                   List<Horaire> nouvelHoraire) {
        EnsembleCours ensemble = ensembleRepository.findById(ensembleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ensemble non trouvé avec l'ID: " + ensembleId));
        
        List<Horaire> horairesExistants = new ArrayList<>();
        for (List<Horaire> horaires : ensemble.getHoraires().values()) {
            horairesExistants.addAll(horaires);
        }
        
        List<ConflitHoraire> conflitsPotentiels = new ArrayList<>();
        for (Horaire nouveau : nouvelHoraire) {
            conflitsPotentiels.addAll(
                    conflitRepository.verifierAjoutHoraire(horairesExistants, nouveau));
        }
        
        Map<String, Object> resultat = new HashMap<>();
        resultat.put("courseId", courseId);
        resultat.put("peutAjouter", conflitsPotentiels.isEmpty());
        resultat.put("nombreConflitsPotentiels", conflitsPotentiels.size());
        resultat.put("conflits", conflitsPotentiels);
        
        if (!conflitsPotentiels.isEmpty()) {
            resultat.put("message", "L'ajout de ce cours créerait " + 
                    conflitsPotentiels.size() + " conflit(s) d'horaire");
        } else {
            resultat.put("message", "Ce cours peut être ajouté sans conflit d'horaire");
        }
        
        return resultat;
    }
    
    /**
     * Vérifie rapidement si un ensemble a des conflits.
     * 
     * @param ensembleId L'ID de l'ensemble
     * @return {@code true} si l'ensemble contient au moins un conflit
     * @throws IllegalArgumentException si l'ensemble n'existe pas
     */
    public boolean hasConflits(String ensembleId) {
        List<ConflitHoraire> conflits = detecterConflits(ensembleId);
        return !conflits.isEmpty();
    }
    
    /**
     * Invalide le cache des conflits pour un ensemble.
     * Doit être appelé après toute modification de l'ensemble.
     * 
     * @param ensembleId L'ID de l'ensemble
     */
    public void invaliderCache(String ensembleId) {
        conflitRepository.invaliderCache(ensembleId);
    }
}
