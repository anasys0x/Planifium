package com.diro.ift2255.service;

import com.diro.ift2255.model.ComparaisonEnsembles;
import com.diro.ift2255.model.ConflitHoraire;
import com.diro.ift2255.model.EnsembleCours;
import com.diro.ift2255.repository.ComparerEnsemblesRepository;
import com.diro.ift2255.repository.EnsembleCoursRepository;

import java.util.*;

/**
 * Service pour la comparaison d'ensembles de cours (CU#12 - Bonus 5%).
 * Implémente la logique métier pour comparer des ensembles selon différents critères.
 * 
 * <p>Ce service permet de comparer deux ou plusieurs ensembles de cours
 * en calculant diverses métriques (crédits, conflits, charge de travail)
 * et en générant des recommandations personnalisées.</p>
 * 
 * <p><b>Fonctionnalités principales :</b></p>
 * <ul>
 *   <li>Comparaison de deux ensembles avec métriques détaillées</li>
 *   <li>Comparaison de plusieurs ensembles simultanément</li>
 *   <li>Génération de tableaux comparatifs</li>
 *   <li>Recommandations personnalisées selon les préférences</li>
 *   <li>Identification du meilleur ensemble</li>
 * </ul>
 * 
 * @author Équipe 10
 * @version 3.0
 * @since 2025-12-27
 * @see ComparaisonEnsembles
 * @see ComparerEnsemblesRepository
 */
public class ComparerEnsemblesService {
    
    /**
     * Repository pour la persistance des comparaisons.
     */
    private final ComparerEnsemblesRepository comparerRepository;
    
    /**
     * Repository pour accéder aux ensembles.
     */
    private final EnsembleCoursRepository ensembleRepository;
    
    /**
     * Service pour la détection des conflits.
     */
    private final ConflitHoraireService conflitService;
    
    /**
     * Constructeur avec injection de dépendances.
     * 
     * @param comparerRepository Le repository des comparaisons
     * @param ensembleRepository Le repository des ensembles
     * @param conflitService Le service de détection des conflits
     */
    public ComparerEnsemblesService(ComparerEnsemblesRepository comparerRepository,
                                     EnsembleCoursRepository ensembleRepository,
                                     ConflitHoraireService conflitService) {
        this.comparerRepository = comparerRepository;
        this.ensembleRepository = ensembleRepository;
        this.conflitService = conflitService;
    }
    
    /**
     * Compare deux ensembles de cours.
     * Utilise le cache si une comparaison existe déjà.
     * 
     * @param ensembleId1 L'ID du premier ensemble
     * @param ensembleId2 L'ID du second ensemble
     * @return Le résultat de la comparaison avec toutes les métriques
     * @throws IllegalArgumentException si l'un des ensembles n'existe pas
     */
    public ComparaisonEnsembles comparerEnsembles(String ensembleId1, String ensembleId2) {
        Optional<ComparaisonEnsembles> cached = comparerRepository.findByEnsembleIds(ensembleId1, ensembleId2);
        if (cached.isPresent()) {
            return cached.get();
        }
        
        EnsembleCours ensemble1 = ensembleRepository.findById(ensembleId1)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ensemble non trouvé avec l'ID: " + ensembleId1));
        
        EnsembleCours ensemble2 = ensembleRepository.findById(ensembleId2)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ensemble non trouvé avec l'ID: " + ensembleId2));
        
        ComparaisonEnsembles comparaison = new ComparaisonEnsembles(ensemble1, ensemble2);
        
        calculerMetriques(comparaison, ensemble1, ensemble2);
        determinerMeilleurEnsemble(comparaison);
        
        return comparerRepository.save(comparaison);
    }
    
    /**
     * Compare plusieurs ensembles et identifie le meilleur.
     * 
     * @param ensembleIds Liste des IDs d'ensembles à comparer
     * @return Map contenant les résultats avec :
     *         - ensembles : informations de chaque ensemble
     *         - meilleurEnsembleId : ID du meilleur ensemble
     *         - nombreEnsemblesCompares : nombre d'ensembles analysés
     * @throws IllegalArgumentException si moins de 2 ensembles sont fournis
     */
    public Map<String, Object> comparerPlusieursEnsembles(List<String> ensembleIds) {
        if (ensembleIds == null || ensembleIds.size() < 2) {
            throw new IllegalArgumentException("Au moins 2 ensembles sont requis pour la comparaison");
        }
        
        Map<String, Object> resultat = new HashMap<>();
        List<Map<String, Object>> ensemblesInfo = new ArrayList<>();
        String meilleurId = null;
        double meilleurScore = Double.MIN_VALUE;
        
        for (String id : ensembleIds) {
            EnsembleCours ensemble = ensembleRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Ensemble non trouvé: " + id));
            
            Map<String, Object> info = calculerInfoEnsemble(ensemble);
            ensemblesInfo.add(info);
            
            double score = (double) info.get("score");
            if (score > meilleurScore) {
                meilleurScore = score;
                meilleurId = id;
            }
        }
        
        resultat.put("ensembles", ensemblesInfo);
        resultat.put("meilleurEnsembleId", meilleurId);
        resultat.put("nombreEnsemblesCompares", ensembleIds.size());
        
        return resultat;
    }
    
    /**
     * Génère un tableau comparatif détaillé entre deux ensembles.
     * 
     * @param ensembleId1 L'ID du premier ensemble
     * @param ensembleId2 L'ID du second ensemble
     * @return Map contenant :
     *         - criteres : liste des critères comparés
     *         - ensemble1 : valeurs pour le premier ensemble
     *         - ensemble2 : valeurs pour le second ensemble
     *         - differences : écarts entre les deux ensembles
     * @throws IllegalArgumentException si l'un des ensembles n'existe pas
     */
    public Map<String, Object> getTableauComparatif(String ensembleId1, String ensembleId2) {
        EnsembleCours ensemble1 = ensembleRepository.findById(ensembleId1)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ensemble non trouvé: " + ensembleId1));
        
        EnsembleCours ensemble2 = ensembleRepository.findById(ensembleId2)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ensemble non trouvé: " + ensembleId2));
        
        Map<String, Object> tableau = new HashMap<>();
        
        List<String> criteres = Arrays.asList(
                "Nombre de cours",
                "Crédits totaux",
                "Conflits d'horaire",
                "Charge estimée",
                "Score global"
        );
        tableau.put("criteres", criteres);
        
        Map<String, Object> valeurs1 = calculerInfoEnsemble(ensemble1);
        tableau.put("ensemble1", valeurs1);
        
        Map<String, Object> valeurs2 = calculerInfoEnsemble(ensemble2);
        tableau.put("ensemble2", valeurs2);
        
        Map<String, Object> differences = new HashMap<>();
        differences.put("nombreCours", (int) valeurs1.get("nombreCours") - (int) valeurs2.get("nombreCours"));
        differences.put("credits", (int) valeurs1.get("credits") - (int) valeurs2.get("credits"));
        differences.put("conflits", (int) valeurs1.get("conflits") - (int) valeurs2.get("conflits"));
        tableau.put("differences", differences);
        
        return tableau;
    }
    
    /**
     * Génère une recommandation personnalisée basée sur les préférences utilisateur.
     * 
     * <p><b>Priorités supportées :</b></p>
     * <ul>
     *   <li>chargeMinimale : favorise les ensembles avec moins de cours</li>
     *   <li>sansConflits : favorise les ensembles sans conflits d'horaire</li>
     *   <li>maxCredits : favorise les ensembles avec plus de crédits</li>
     *   <li>equilibre : score équilibré (défaut)</li>
     * </ul>
     * 
     * @param ensembleIds Liste des IDs d'ensembles à évaluer
     * @param preferences Map des préférences avec :
     *                    - priorite : type de priorité
     *                    - maxCours : nombre maximum de cours souhaité
     * @return Map contenant la recommandation avec raison
     * @throws IllegalArgumentException si aucun ensemble n'est fourni
     */
    public Map<String, Object> genererRecommandationPersonnalisee(List<String> ensembleIds,
                                                                   Map<String, Object> preferences) {
        if (ensembleIds == null || ensembleIds.isEmpty()) {
            throw new IllegalArgumentException("Au moins un ensemble est requis");
        }
        
        String priorite = preferences != null ? 
                (String) preferences.getOrDefault("priorite", "equilibre") : "equilibre";
        Integer maxCours = preferences != null ? 
                (Integer) preferences.get("maxCours") : null;
        
        Map<String, Object> recommandation = new HashMap<>();
        String meilleurId = null;
        double meilleurScore = Double.MIN_VALUE;
        String raison = "";
        
        for (String id : ensembleIds) {
            EnsembleCours ensemble = ensembleRepository.findById(id).orElse(null);
            if (ensemble == null) continue;
            
            if (maxCours != null && ensemble.getNombreCours() > maxCours) {
                continue;
            }
            
            double score = calculerScorePersonnalise(ensemble, priorite);
            
            if (score > meilleurScore) {
                meilleurScore = score;
                meilleurId = id;
                raison = genererRaison(ensemble, priorite);
            }
        }
        
        recommandation.put("ensembleRecommande", meilleurId);
        recommandation.put("score", meilleurScore);
        recommandation.put("raison", raison);
        recommandation.put("prioriteUtilisee", priorite);
        
        return recommandation;
    }
    
    /**
     * Récupère toutes les comparaisons impliquant un ensemble.
     * 
     * @param ensembleId L'ID de l'ensemble
     * @return Liste des comparaisons existantes
     */
    public List<ComparaisonEnsembles> getComparaisonsPourEnsemble(String ensembleId) {
        return comparerRepository.findByEnsembleId(ensembleId);
    }
    
    /**
     * Invalide le cache de comparaison pour un ensemble.
     * Supprime toutes les comparaisons impliquant cet ensemble.
     * 
     * @param ensembleId L'ID de l'ensemble
     */
    public void invaliderCache(String ensembleId) {
        comparerRepository.invalidate(ensembleId);
    }
    
    /**
     * Calcule les métriques de comparaison entre deux ensembles.
     * 
     * @param comparaison L'objet comparaison à remplir
     * @param e1 Premier ensemble
     * @param e2 Second ensemble
     */
    private void calculerMetriques(ComparaisonEnsembles comparaison, 
                                    EnsembleCours e1, EnsembleCours e2) {
        comparaison.setCreditsEnsemble1(e1.getNombreCours() * 3);
        comparaison.setCreditsEnsemble2(e2.getNombreCours() * 3);
        
        try {
            List<ConflitHoraire> conflits1 = conflitService.detecterConflits(e1.getId());
            comparaison.setConflitsEnsemble1(conflits1.size());
        } catch (Exception ex) {
            comparaison.setConflitsEnsemble1(0);
        }
        
        try {
            List<ConflitHoraire> conflits2 = conflitService.detecterConflits(e2.getId());
            comparaison.setConflitsEnsemble2(conflits2.size());
        } catch (Exception ex) {
            comparaison.setConflitsEnsemble2(0);
        }
        
        comparaison.setChargeEstimeeEnsemble1(e1.getNombreCours() * 10.0);
        comparaison.setChargeEstimeeEnsemble2(e2.getNombreCours() * 10.0);
        
        double score1 = calculerScore(e1, comparaison.getConflitsEnsemble1());
        double score2 = calculerScore(e2, comparaison.getConflitsEnsemble2());
        comparaison.setScoreEnsemble1(score1);
        comparaison.setScoreEnsemble2(score2);
    }
    
    /**
     * Détermine le meilleur ensemble basé sur les scores.
     * 
     * @param comparaison La comparaison à compléter
     */
    private void determinerMeilleurEnsemble(ComparaisonEnsembles comparaison) {
        double score1 = comparaison.getScoreEnsemble1();
        double score2 = comparaison.getScoreEnsemble2();
        
        if (score1 > score2) {
            comparaison.setMeilleurEnsembleId(comparaison.getEnsemble1().getId());
            comparaison.setRaisonRecommandation(genererRaisonComparaison(comparaison, true));
        } else if (score2 > score1) {
            comparaison.setMeilleurEnsembleId(comparaison.getEnsemble2().getId());
            comparaison.setRaisonRecommandation(genererRaisonComparaison(comparaison, false));
        } else {
            comparaison.setMeilleurEnsembleId(comparaison.getEnsemble1().getId());
            comparaison.setRaisonRecommandation("Les deux ensembles sont équivalents");
        }
    }
    
    /**
     * Calcule le score global d'un ensemble.
     * Score basé sur le nombre de cours (positif) et les conflits (pénalité).
     * 
     * @param ensemble L'ensemble à évaluer
     * @param conflits Le nombre de conflits
     * @return Le score calculé
     */
    private double calculerScore(EnsembleCours ensemble, int conflits) {
        double scoreBase = ensemble.getNombreCours() * 10;
        double penaliteConflits = conflits * 20;
        return Math.max(0, scoreBase - penaliteConflits);
    }
    
    /**
     * Calcule un score personnalisé selon la priorité.
     * 
     * @param ensemble L'ensemble à évaluer
     * @param priorite La priorité utilisateur
     * @return Le score personnalisé
     */
    private double calculerScorePersonnalise(EnsembleCours ensemble, String priorite) {
        int conflits = 0;
        try {
            conflits = conflitService.detecterConflits(ensemble.getId()).size();
        } catch (Exception e) {
            // Ignorer
        }
        
        switch (priorite.toLowerCase()) {
            case "chargeminimale":
                return 100 - (ensemble.getNombreCours() * 15) - (conflits * 10);
            case "sansconflits":
                return 100 - (conflits * 30);
            case "maxcredits":
                return ensemble.getNombreCours() * 15 - (conflits * 5);
            case "equilibre":
            default:
                return calculerScore(ensemble, conflits);
        }
    }
    
    /**
     * Calcule les informations résumées d'un ensemble.
     * 
     * @param ensemble L'ensemble à analyser
     * @return Map des informations
     */
    private Map<String, Object> calculerInfoEnsemble(EnsembleCours ensemble) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", ensemble.getId());
        info.put("nom", ensemble.getNom());
        info.put("nombreCours", ensemble.getNombreCours());
        info.put("credits", ensemble.getNombreCours() * 3);
        
        int conflits = 0;
        try {
            conflits = conflitService.detecterConflits(ensemble.getId()).size();
        } catch (Exception e) {
            // Ignorer
        }
        info.put("conflits", conflits);
        
        double score = calculerScore(ensemble, conflits);
        info.put("score", score);
        
        return info;
    }
    
    /**
     * Génère la raison de recommandation pour une comparaison.
     * 
     * @param comp La comparaison
     * @param ensemble1Meilleur Indique si l'ensemble 1 est meilleur
     * @return La raison textuelle
     */
    private String genererRaisonComparaison(ComparaisonEnsembles comp, boolean ensemble1Meilleur) {
        List<String> raisons = new ArrayList<>();
        
        if (ensemble1Meilleur) {
            if (comp.getConflitsEnsemble1() < comp.getConflitsEnsemble2()) {
                raisons.add("moins de conflits d'horaire");
            }
            if (comp.getCreditsEnsemble1() > comp.getCreditsEnsemble2()) {
                raisons.add("plus de crédits");
            }
        } else {
            if (comp.getConflitsEnsemble2() < comp.getConflitsEnsemble1()) {
                raisons.add("moins de conflits d'horaire");
            }
            if (comp.getCreditsEnsemble2() > comp.getCreditsEnsemble1()) {
                raisons.add("plus de crédits");
            }
        }
        
        if (raisons.isEmpty()) {
            return "Meilleur score global";
        }
        
        return String.join(", ", raisons);
    }
    
    /**
     * Génère la raison pour une recommandation personnalisée.
     * 
     * @param ensemble L'ensemble recommandé
     * @param priorite La priorité utilisée
     * @return La raison textuelle
     */
    private String genererRaison(EnsembleCours ensemble, String priorite) {
        switch (priorite.toLowerCase()) {
            case "chargeminimale":
                return "Charge de travail minimale avec " + ensemble.getNombreCours() + " cours";
            case "sansconflits":
                return "Aucun ou minimum de conflits d'horaire";
            case "maxcredits":
                return "Maximum de crédits avec " + (ensemble.getNombreCours() * 3) + " crédits";
            default:
                return "Meilleur équilibre global";
        }
    }
}
