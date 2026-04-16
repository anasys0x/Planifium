package com.diro.ift2255.model;

import java.util.Date;
import java.util.Objects;

/**
 * Représente le résultat d'une comparaison entre deux ensembles de cours.
 * Contient les métriques de comparaison et les recommandations.
 * 
 * <p>Cette classe stocke toutes les informations nécessaires pour comparer
 * deux ensembles de cours, incluant les crédits, les conflits, la charge
 * estimée et les scores calculés pour chaque ensemble.</p>
 * 
 * @author Équipe 10
 * @version 3.0
 * @since 2025-12-27
 * @see EnsembleCours
 * @see com.diro.ift2255.service.ComparerEnsemblesService
 */
public class ComparaisonEnsembles {
    
    /**
     * Identifiant unique de la comparaison.
     * Format: "CMP-{id1}-{id2}" avec ordre lexicographique.
     */
    private String id;
    
    /**
     * Premier ensemble comparé.
     */
    private EnsembleCours ensemble1;
    
    /**
     * Second ensemble comparé.
     */
    private EnsembleCours ensemble2;
    
    /**
     * Nombre total de crédits pour l'ensemble 1.
     */
    private int creditsEnsemble1;
    
    /**
     * Nombre de conflits d'horaire dans l'ensemble 1.
     */
    private int conflitsEnsemble1;
    
    /**
     * Charge de travail estimée pour l'ensemble 1 (heures/semaine).
     */
    private double chargeEstimeeEnsemble1;
    
    /**
     * Difficulté estimée pour l'ensemble 1 (échelle de 1 à 5).
     */
    private double difficulteEstimeeEnsemble1;
    
    /**
     * Nombre total de crédits pour l'ensemble 2.
     */
    private int creditsEnsemble2;
    
    /**
     * Nombre de conflits d'horaire dans l'ensemble 2.
     */
    private int conflitsEnsemble2;
    
    /**
     * Charge de travail estimée pour l'ensemble 2 (heures/semaine).
     */
    private double chargeEstimeeEnsemble2;
    
    /**
     * Difficulté estimée pour l'ensemble 2 (échelle de 1 à 5).
     */
    private double difficulteEstimeeEnsemble2;
    
    /**
     * ID de l'ensemble recommandé comme meilleur choix.
     */
    private String meilleurEnsembleId;
    
    /**
     * Explication de la recommandation.
     */
    private String raisonRecommandation;
    
    /**
     * Score global calculé pour l'ensemble 1.
     */
    private double scoreEnsemble1;
    
    /**
     * Score global calculé pour l'ensemble 2.
     */
    private double scoreEnsemble2;
    
    /**
     * Date à laquelle la comparaison a été effectuée.
     */
    private Date dateComparaison;
    
    /**
     * Constructeur par défaut.
     * Initialise la date de comparaison à maintenant.
     */
    public ComparaisonEnsembles() {
        this.dateComparaison = new Date();
    }
    
    /**
     * Constructeur avec les deux ensembles à comparer.
     * Génère automatiquement l'ID de la comparaison.
     * 
     * @param ensemble1 Le premier ensemble
     * @param ensemble2 Le second ensemble
     */
    public ComparaisonEnsembles(EnsembleCours ensemble1, EnsembleCours ensemble2) {
        this();
        this.ensemble1 = ensemble1;
        this.ensemble2 = ensemble2;
        this.id = genererIdComparaison(ensemble1, ensemble2);
    }
    
    /**
     * Génère un ID unique pour la comparaison.
     * Utilise l'ordre lexicographique pour garantir la cohérence.
     * 
     * @param e1 Premier ensemble
     * @param e2 Second ensemble
     * @return L'ID généré
     */
    private String genererIdComparaison(EnsembleCours e1, EnsembleCours e2) {
        if (e1 == null || e2 == null) return null;
        String id1 = e1.getId();
        String id2 = e2.getId();
        if (id1.compareTo(id2) <= 0) {
            return "CMP-" + id1 + "-" + id2;
        } else {
            return "CMP-" + id2 + "-" + id1;
        }
    }
    
    /**
     * Retourne l'identifiant de la comparaison.
     * 
     * @return L'ID de la comparaison
     */
    public String getId() {
        return id;
    }
    
    /**
     * Définit l'identifiant de la comparaison.
     * 
     * @param id Le nouvel ID
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Retourne le premier ensemble.
     * 
     * @return Le premier ensemble
     */
    public EnsembleCours getEnsemble1() {
        return ensemble1;
    }
    
    /**
     * Définit le premier ensemble.
     * 
     * @param ensemble1 Le premier ensemble
     */
    public void setEnsemble1(EnsembleCours ensemble1) {
        this.ensemble1 = ensemble1;
    }
    
    /**
     * Retourne le second ensemble.
     * 
     * @return Le second ensemble
     */
    public EnsembleCours getEnsemble2() {
        return ensemble2;
    }
    
    /**
     * Définit le second ensemble.
     * 
     * @param ensemble2 Le second ensemble
     */
    public void setEnsemble2(EnsembleCours ensemble2) {
        this.ensemble2 = ensemble2;
    }
    
    /**
     * Retourne le nombre de crédits de l'ensemble 1.
     * 
     * @return Les crédits de l'ensemble 1
     */
    public int getCreditsEnsemble1() {
        return creditsEnsemble1;
    }
    
    /**
     * Définit le nombre de crédits de l'ensemble 1.
     * 
     * @param creditsEnsemble1 Les crédits
     */
    public void setCreditsEnsemble1(int creditsEnsemble1) {
        this.creditsEnsemble1 = creditsEnsemble1;
    }
    
    /**
     * Retourne le nombre de conflits de l'ensemble 1.
     * 
     * @return Le nombre de conflits
     */
    public int getConflitsEnsemble1() {
        return conflitsEnsemble1;
    }
    
    /**
     * Définit le nombre de conflits de l'ensemble 1.
     * 
     * @param conflitsEnsemble1 Le nombre de conflits
     */
    public void setConflitsEnsemble1(int conflitsEnsemble1) {
        this.conflitsEnsemble1 = conflitsEnsemble1;
    }
    
    /**
     * Retourne la charge estimée de l'ensemble 1.
     * 
     * @return La charge en heures/semaine
     */
    public double getChargeEstimeeEnsemble1() {
        return chargeEstimeeEnsemble1;
    }
    
    /**
     * Définit la charge estimée de l'ensemble 1.
     * 
     * @param chargeEstimeeEnsemble1 La charge en heures/semaine
     */
    public void setChargeEstimeeEnsemble1(double chargeEstimeeEnsemble1) {
        this.chargeEstimeeEnsemble1 = chargeEstimeeEnsemble1;
    }
    
    /**
     * Retourne la difficulté estimée de l'ensemble 1.
     * 
     * @return La difficulté (1-5)
     */
    public double getDifficulteEstimeeEnsemble1() {
        return difficulteEstimeeEnsemble1;
    }
    
    /**
     * Définit la difficulté estimée de l'ensemble 1.
     * 
     * @param difficulteEstimeeEnsemble1 La difficulté
     */
    public void setDifficulteEstimeeEnsemble1(double difficulteEstimeeEnsemble1) {
        this.difficulteEstimeeEnsemble1 = difficulteEstimeeEnsemble1;
    }
    
    /**
     * Retourne le nombre de crédits de l'ensemble 2.
     * 
     * @return Les crédits de l'ensemble 2
     */
    public int getCreditsEnsemble2() {
        return creditsEnsemble2;
    }
    
    /**
     * Définit le nombre de crédits de l'ensemble 2.
     * 
     * @param creditsEnsemble2 Les crédits
     */
    public void setCreditsEnsemble2(int creditsEnsemble2) {
        this.creditsEnsemble2 = creditsEnsemble2;
    }
    
    /**
     * Retourne le nombre de conflits de l'ensemble 2.
     * 
     * @return Le nombre de conflits
     */
    public int getConflitsEnsemble2() {
        return conflitsEnsemble2;
    }
    
    /**
     * Définit le nombre de conflits de l'ensemble 2.
     * 
     * @param conflitsEnsemble2 Le nombre de conflits
     */
    public void setConflitsEnsemble2(int conflitsEnsemble2) {
        this.conflitsEnsemble2 = conflitsEnsemble2;
    }
    
    /**
     * Retourne la charge estimée de l'ensemble 2.
     * 
     * @return La charge en heures/semaine
     */
    public double getChargeEstimeeEnsemble2() {
        return chargeEstimeeEnsemble2;
    }
    
    /**
     * Définit la charge estimée de l'ensemble 2.
     * 
     * @param chargeEstimeeEnsemble2 La charge en heures/semaine
     */
    public void setChargeEstimeeEnsemble2(double chargeEstimeeEnsemble2) {
        this.chargeEstimeeEnsemble2 = chargeEstimeeEnsemble2;
    }
    
    /**
     * Retourne la difficulté estimée de l'ensemble 2.
     * 
     * @return La difficulté (1-5)
     */
    public double getDifficulteEstimeeEnsemble2() {
        return difficulteEstimeeEnsemble2;
    }
    
    /**
     * Définit la difficulté estimée de l'ensemble 2.
     * 
     * @param difficulteEstimeeEnsemble2 La difficulté
     */
    public void setDifficulteEstimeeEnsemble2(double difficulteEstimeeEnsemble2) {
        this.difficulteEstimeeEnsemble2 = difficulteEstimeeEnsemble2;
    }
    
    /**
     * Retourne l'ID du meilleur ensemble recommandé.
     * 
     * @return L'ID du meilleur ensemble
     */
    public String getMeilleurEnsembleId() {
        return meilleurEnsembleId;
    }
    
    /**
     * Définit l'ID du meilleur ensemble.
     * 
     * @param meilleurEnsembleId L'ID du meilleur ensemble
     */
    public void setMeilleurEnsembleId(String meilleurEnsembleId) {
        this.meilleurEnsembleId = meilleurEnsembleId;
    }
    
    /**
     * Retourne la raison de la recommandation.
     * 
     * @return L'explication de la recommandation
     */
    public String getRaisonRecommandation() {
        return raisonRecommandation;
    }
    
    /**
     * Définit la raison de la recommandation.
     * 
     * @param raisonRecommandation L'explication
     */
    public void setRaisonRecommandation(String raisonRecommandation) {
        this.raisonRecommandation = raisonRecommandation;
    }
    
    /**
     * Retourne le score de l'ensemble 1.
     * 
     * @return Le score calculé
     */
    public double getScoreEnsemble1() {
        return scoreEnsemble1;
    }
    
    /**
     * Définit le score de l'ensemble 1.
     * 
     * @param scoreEnsemble1 Le score
     */
    public void setScoreEnsemble1(double scoreEnsemble1) {
        this.scoreEnsemble1 = scoreEnsemble1;
    }
    
    /**
     * Retourne le score de l'ensemble 2.
     * 
     * @return Le score calculé
     */
    public double getScoreEnsemble2() {
        return scoreEnsemble2;
    }
    
    /**
     * Définit le score de l'ensemble 2.
     * 
     * @param scoreEnsemble2 Le score
     */
    public void setScoreEnsemble2(double scoreEnsemble2) {
        this.scoreEnsemble2 = scoreEnsemble2;
    }
    
    /**
     * Retourne la date de la comparaison.
     * 
     * @return La date de comparaison
     */
    public Date getDateComparaison() {
        return dateComparaison;
    }
    
    /**
     * Définit la date de la comparaison.
     * 
     * @param dateComparaison La date
     */
    public void setDateComparaison(Date dateComparaison) {
        this.dateComparaison = dateComparaison;
    }
    
    /**
     * Calcule la différence de crédits entre les deux ensembles.
     * 
     * @return La différence (ensemble1 - ensemble2)
     */
    public int getDifferenceCredits() {
        return creditsEnsemble1 - creditsEnsemble2;
    }
    
    /**
     * Calcule la différence de conflits entre les deux ensembles.
     * 
     * @return La différence (ensemble1 - ensemble2)
     */
    public int getDifferenceConflits() {
        return conflitsEnsemble1 - conflitsEnsemble2;
    }
    
    /**
     * Retourne une représentation textuelle de la comparaison.
     * 
     * @return Une chaîne décrivant la comparaison
     */
    @Override
    public String toString() {
        return "ComparaisonEnsembles{" +
                "id='" + id + '\'' +
                ", ensemble1=" + (ensemble1 != null ? ensemble1.getId() : "null") +
                ", ensemble2=" + (ensemble2 != null ? ensemble2.getId() : "null") +
                ", meilleurEnsembleId='" + meilleurEnsembleId + '\'' +
                '}';
    }
    
    /**
     * Vérifie l'égalité avec un autre objet.
     * 
     * @param o L'objet à comparer
     * @return {@code true} si les comparaisons sont égales
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComparaisonEnsembles that = (ComparaisonEnsembles) o;
        return Objects.equals(id, that.id);
    }
    
    /**
     * Calcule le code de hachage.
     * 
     * @return Le code de hachage
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
