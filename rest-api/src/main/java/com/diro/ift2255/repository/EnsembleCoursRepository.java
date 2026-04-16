package com.diro.ift2255.repository;

import com.diro.ift2255.model.EnsembleCours;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Repository pour la gestion de la persistance des ensembles de cours.
 * Fournit les opérations CRUD avec stockage en mémoire.
 * 
 * <p>Cette classe utilise un {@link ConcurrentHashMap} pour le stockage
 * thread-safe des ensembles et un {@link AtomicInteger} pour la génération
 * d'identifiants uniques.</p>
 * 
 * @author Équipe 10
 * @version 3.0
 * @since 2025-12-27
 * @see EnsembleCours
 * @see com.diro.ift2255.service.EnsembleCoursService
 */
public class EnsembleCoursRepository {
    
    /**
     * Stockage en mémoire des ensembles, indexé par ID.
     */
    private final Map<String, EnsembleCours> ensembles;
    
    /**
     * Compteur atomique pour la génération d'IDs uniques.
     */
    private final AtomicInteger idCounter;
    
    /**
     * Constructeur par défaut.
     * Initialise le stockage vide et le compteur à 1.
     */
    public EnsembleCoursRepository() {
        this.ensembles = new ConcurrentHashMap<>();
        this.idCounter = new AtomicInteger(1);
    }
    
    /**
     * Sauvegarde un nouvel ensemble ou met à jour un existant.
     * Si l'ensemble n'a pas d'ID, un nouvel ID est généré automatiquement.
     * 
     * @param ensemble L'ensemble à sauvegarder
     * @return L'ensemble sauvegardé avec son ID
     * @throws IllegalArgumentException si l'ensemble est null ou contient plus de 6 cours
     */
    public EnsembleCours save(EnsembleCours ensemble) {
        if (ensemble == null) {
            throw new IllegalArgumentException("L'ensemble ne peut pas être null");
        }
        
        if (ensemble.getCoursIds() != null && ensemble.getCoursIds().size() > EnsembleCours.MAX_COURS) {
            throw new IllegalArgumentException("Un ensemble ne peut contenir plus de " + 
                    EnsembleCours.MAX_COURS + " cours.");
        }
        
        if (ensemble.getId() == null || ensemble.getId().isEmpty()) {
            String newId = String.format("ENS-%05d", idCounter.getAndIncrement());
            ensemble.setId(newId);
        }
        
        ensembles.put(ensemble.getId(), ensemble);
        return ensemble;
    }
    
    /**
     * Recherche un ensemble par son identifiant.
     * 
     * @param id L'identifiant de l'ensemble recherché
     * @return Un {@link Optional} contenant l'ensemble si trouvé, vide sinon
     */
    public Optional<EnsembleCours> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(ensembles.get(id));
    }
    
    /**
     * Retourne tous les ensembles enregistrés.
     * 
     * @return Une liste de tous les ensembles
     */
    public List<EnsembleCours> findAll() {
        return new ArrayList<>(ensembles.values());
    }
    
    /**
     * Recherche les ensembles pour un trimestre donné.
     * 
     * @param trimestre Le code du trimestre (ex: "A25")
     * @return Liste des ensembles correspondants
     */
    public List<EnsembleCours> findByTrimestre(String trimestre) {
        if (trimestre == null) {
            return new ArrayList<>();
        }
        
        String normalizedTrimestre = trimestre.toUpperCase();
        return ensembles.values().stream()
                .filter(e -> normalizedTrimestre.equals(e.getTrimestre()))
                .collect(Collectors.toList());
    }
    
    /**
     * Met à jour un ensemble existant.
     * 
     * @param ensemble L'ensemble avec les nouvelles valeurs
     * @return L'ensemble mis à jour
     * @throws IllegalArgumentException si l'ensemble n'existe pas ou est invalide
     */
    public EnsembleCours update(EnsembleCours ensemble) {
        if (ensemble == null || ensemble.getId() == null) {
            throw new IllegalArgumentException("L'ensemble et son ID ne peuvent pas être null");
        }
        
        if (!ensembles.containsKey(ensemble.getId())) {
            throw new IllegalArgumentException("Ensemble non trouvé avec l'ID: " + ensemble.getId());
        }
        
        if (ensemble.getCoursIds() != null && ensemble.getCoursIds().size() > EnsembleCours.MAX_COURS) {
            throw new IllegalArgumentException("Un ensemble ne peut contenir plus de " + 
                    EnsembleCours.MAX_COURS + " cours.");
        }
        
        ensembles.put(ensemble.getId(), ensemble);
        return ensemble;
    }
    
    /**
     * Supprime un ensemble par son identifiant.
     * 
     * @param id L'identifiant de l'ensemble à supprimer
     * @return {@code true} si l'ensemble a été supprimé, {@code false} s'il n'existait pas
     */
    public boolean deleteById(String id) {
        if (id == null) {
            return false;
        }
        return ensembles.remove(id) != null;
    }
    
    /**
     * Vérifie si un ensemble existe.
     * 
     * @param id L'identifiant à vérifier
     * @return {@code true} si l'ensemble existe
     */
    public boolean existsById(String id) {
        return id != null && ensembles.containsKey(id);
    }
    
    /**
     * Compte le nombre total d'ensembles enregistrés.
     * 
     * @return Le nombre d'ensembles
     */
    public long count() {
        return ensembles.size();
    }
    
    /**
     * Recherche les ensembles contenant un cours spécifique.
     * 
     * @param coursId L'identifiant du cours recherché
     * @return Liste des ensembles contenant ce cours
     */
    public List<EnsembleCours> findByCoursId(String coursId) {
        if (coursId == null) {
            return new ArrayList<>();
        }
        
        String normalizedId = coursId.toUpperCase();
        return ensembles.values().stream()
                .filter(e -> e.getCoursIds().contains(normalizedId))
                .collect(Collectors.toList());
    }
    
    /**
     * Supprime tous les ensembles.
     * Utilisé principalement pour les tests.
     */
    public void deleteAll() {
        ensembles.clear();
    }
}
