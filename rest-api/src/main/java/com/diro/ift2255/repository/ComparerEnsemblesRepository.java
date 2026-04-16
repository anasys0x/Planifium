package com.diro.ift2255.repository;

import com.diro.ift2255.model.ComparaisonEnsembles;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Repository pour la gestion des résultats de comparaison d'ensembles.
 * Stocke et met en cache les comparaisons effectuées.
 * 
 * <p>Cette classe utilise une clé bidirectionnelle pour garantir que
 * la comparaison entre A et B soit identique à celle entre B et A.
 * L'ordre lexicographique des IDs est utilisé pour générer la clé.</p>
 * 
 * @author Équipe 10
 * @version 3.0
 * @since 2025-12-27
 * @see ComparaisonEnsembles
 * @see com.diro.ift2255.service.ComparerEnsemblesService
 */
public class ComparerEnsemblesRepository {
    
    /**
     * Stockage des comparaisons indexé par clé de comparaison.
     */
    private final Map<String, ComparaisonEnsembles> comparaisons;
    
    /**
     * Constructeur par défaut.
     * Initialise le stockage vide.
     */
    public ComparerEnsemblesRepository() {
        this.comparaisons = new ConcurrentHashMap<>();
    }
    
    /**
     * Sauvegarde une comparaison.
     * Génère automatiquement l'ID si non présent.
     * 
     * @param comparaison La comparaison à sauvegarder
     * @return La comparaison sauvegardée
     * @throws IllegalArgumentException si la comparaison est null
     */
    public ComparaisonEnsembles save(ComparaisonEnsembles comparaison) {
        if (comparaison == null) {
            throw new IllegalArgumentException("La comparaison ne peut pas être null");
        }
        
        if (comparaison.getId() == null) {
            String id = genererCle(
                    comparaison.getEnsemble1() != null ? comparaison.getEnsemble1().getId() : null,
                    comparaison.getEnsemble2() != null ? comparaison.getEnsemble2().getId() : null
            );
            comparaison.setId(id);
        }
        
        comparaisons.put(comparaison.getId(), comparaison);
        return comparaison;
    }
    
    /**
     * Recherche une comparaison par les IDs des deux ensembles.
     * L'ordre des paramètres n'a pas d'importance.
     * 
     * @param ensembleId1 ID du premier ensemble
     * @param ensembleId2 ID du second ensemble
     * @return Un {@link Optional} contenant la comparaison si trouvée
     */
    public Optional<ComparaisonEnsembles> findByEnsembleIds(String ensembleId1, String ensembleId2) {
        String cle = genererCle(ensembleId1, ensembleId2);
        return Optional.ofNullable(comparaisons.get(cle));
    }
    
    /**
     * Recherche toutes les comparaisons impliquant un ensemble donné.
     * 
     * @param ensembleId L'ID de l'ensemble recherché
     * @return Liste des comparaisons impliquant cet ensemble
     */
    public List<ComparaisonEnsembles> findByEnsembleId(String ensembleId) {
        if (ensembleId == null) {
            return new ArrayList<>();
        }
        
        return comparaisons.values().stream()
                .filter(c -> 
                    (c.getEnsemble1() != null && ensembleId.equals(c.getEnsemble1().getId())) ||
                    (c.getEnsemble2() != null && ensembleId.equals(c.getEnsemble2().getId()))
                )
                .collect(Collectors.toList());
    }
    
    /**
     * Retourne toutes les comparaisons enregistrées.
     * 
     * @return Liste de toutes les comparaisons
     */
    public List<ComparaisonEnsembles> findAll() {
        return new ArrayList<>(comparaisons.values());
    }
    
    /**
     * Supprime une comparaison entre deux ensembles.
     * 
     * @param ensembleId1 ID du premier ensemble
     * @param ensembleId2 ID du second ensemble
     * @return {@code true} si la comparaison a été supprimée
     */
    public boolean delete(String ensembleId1, String ensembleId2) {
        String cle = genererCle(ensembleId1, ensembleId2);
        return comparaisons.remove(cle) != null;
    }
    
    /**
     * Invalide le cache pour un ensemble.
     * Supprime toutes les comparaisons impliquant cet ensemble.
     * 
     * @param ensembleId L'ID de l'ensemble
     */
    public void invalidate(String ensembleId) {
        if (ensembleId == null) return;
        
        List<String> clesToRemove = comparaisons.entrySet().stream()
                .filter(e -> {
                    ComparaisonEnsembles c = e.getValue();
                    return (c.getEnsemble1() != null && ensembleId.equals(c.getEnsemble1().getId())) ||
                           (c.getEnsemble2() != null && ensembleId.equals(c.getEnsemble2().getId()));
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        clesToRemove.forEach(comparaisons::remove);
    }
    
    /**
     * Génère une clé unique pour une paire d'ensembles.
     * Utilise l'ordre lexicographique pour garantir la bidirectionnalité.
     * 
     * @param id1 ID du premier ensemble
     * @param id2 ID du second ensemble
     * @return La clé générée au format "CMP-{id1}-{id2}"
     */
    private String genererCle(String id1, String id2) {
        if (id1 == null || id2 == null) {
            return "CMP-NULL";
        }
        
        if (id1.compareTo(id2) <= 0) {
            return "CMP-" + id1 + "-" + id2;
        } else {
            return "CMP-" + id2 + "-" + id1;
        }
    }
    
    /**
     * Vérifie si une comparaison existe entre deux ensembles.
     * 
     * @param ensembleId1 ID du premier ensemble
     * @param ensembleId2 ID du second ensemble
     * @return {@code true} si la comparaison existe
     */
    public boolean exists(String ensembleId1, String ensembleId2) {
        return findByEnsembleIds(ensembleId1, ensembleId2).isPresent();
    }
    
    /**
     * Compte le nombre total de comparaisons enregistrées.
     * 
     * @return Le nombre de comparaisons
     */
    public long count() {
        return comparaisons.size();
    }
    
    /**
     * Supprime toutes les comparaisons.
     */
    public void deleteAll() {
        comparaisons.clear();
    }
}
