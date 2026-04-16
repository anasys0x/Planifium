package com.diro.ift2255.model;

import java.util.Objects;

/**
 * Représente un conflit d'horaire entre deux créneaux.
 * Un conflit peut être total (horaires identiques) ou partiel (chevauchement partiel).
 * 
 * <p>Cette classe est générée lors de la détection de conflits entre les horaires
 * des cours d'un ensemble. Elle contient les informations sur les deux horaires
 * en conflit ainsi que les détails du chevauchement.</p>
 * 
 * @author Équipe 10
 * @version 3.0
 * @since 2025-12-27
 * @see Horaire
 * @see com.diro.ift2255.service.ConflitHoraireService
 */
public class ConflitHoraire {
    
    /**
     * Énumération des types de conflit possibles.
     */
    public enum TypeConflit {
        /**
         * Chevauchement total - les horaires sont identiques.
         */
        TOTAL,
        
        /**
         * Chevauchement partiel - les horaires se chevauchent partiellement.
         */
        PARTIEL
    }
    
    /**
     * Premier horaire impliqué dans le conflit.
     */
    private Horaire horaire1;
    
    /**
     * Second horaire impliqué dans le conflit.
     */
    private Horaire horaire2;
    
    /**
     * Type de conflit (TOTAL ou PARTIEL).
     */
    private TypeConflit type;
    
    /**
     * Durée du chevauchement en minutes.
     */
    private long dureeChevauchementsMinutes;
    
    /**
     * Description textuelle du conflit.
     */
    private String description;
    
    /**
     * Constructeur par défaut.
     */
    public ConflitHoraire() {
    }
    
    /**
     * Constructeur avec les deux horaires en conflit.
     * Le type est déterminé automatiquement.
     * 
     * @param horaire1 Le premier horaire
     * @param horaire2 Le second horaire
     */
    public ConflitHoraire(Horaire horaire1, Horaire horaire2) {
        this.horaire1 = horaire1;
        this.horaire2 = horaire2;
        this.type = determinerType(horaire1, horaire2);
        this.dureeChevauchementsMinutes = calculerDuree(horaire1, horaire2);
        this.description = genererDescription();
    }
    
    /**
     * Constructeur avec type explicite.
     * 
     * @param horaire1 Le premier horaire
     * @param horaire2 Le second horaire
     * @param type Le type de conflit
     */
    public ConflitHoraire(Horaire horaire1, Horaire horaire2, TypeConflit type) {
        this.horaire1 = horaire1;
        this.horaire2 = horaire2;
        this.type = type;
        this.dureeChevauchementsMinutes = calculerDuree(horaire1, horaire2);
        this.description = genererDescription();
    }
    
    /**
     * Détermine le type de conflit entre deux horaires.
     * 
     * @param h1 Premier horaire
     * @param h2 Second horaire
     * @return Le type de conflit
     */
    private TypeConflit determinerType(Horaire h1, Horaire h2) {
        if (h1 == null || h2 == null) return TypeConflit.PARTIEL;
        
        if (h1.estChevauchementTotal(h2)) {
            return TypeConflit.TOTAL;
        }
        return TypeConflit.PARTIEL;
    }
    
    /**
     * Calcule la durée du chevauchement en minutes.
     * 
     * @param h1 Premier horaire
     * @param h2 Second horaire
     * @return La durée en minutes
     */
    private long calculerDuree(Horaire h1, Horaire h2) {
        if (h1 == null || h2 == null) return 0;
        return h1.getDureeChevauchementsMinutes(h2);
    }
    
    /**
     * Génère une description textuelle du conflit.
     * 
     * @return La description du conflit
     */
    private String genererDescription() {
        if (horaire1 == null || horaire2 == null) {
            return "Conflit non défini";
        }
        
        String typeStr = type == TypeConflit.TOTAL ? "total" : "partiel";
        return String.format("Conflit %s entre %s et %s le %s (%d minutes)",
                typeStr,
                horaire1.getCourseId(),
                horaire2.getCourseId(),
                horaire1.getJourString(),
                dureeChevauchementsMinutes);
    }
    
    /**
     * Retourne le premier horaire du conflit.
     * 
     * @return Le premier horaire
     */
    public Horaire getHoraire1() {
        return horaire1;
    }
    
    /**
     * Définit le premier horaire du conflit.
     * 
     * @param horaire1 Le premier horaire
     */
    public void setHoraire1(Horaire horaire1) {
        this.horaire1 = horaire1;
    }
    
    /**
     * Retourne le second horaire du conflit.
     * 
     * @return Le second horaire
     */
    public Horaire getHoraire2() {
        return horaire2;
    }
    
    /**
     * Définit le second horaire du conflit.
     * 
     * @param horaire2 Le second horaire
     */
    public void setHoraire2(Horaire horaire2) {
        this.horaire2 = horaire2;
    }
    
    /**
     * Retourne le type de conflit.
     * 
     * @return Le type de conflit
     */
    public TypeConflit getType() {
        return type;
    }
    
    /**
     * Définit le type de conflit.
     * 
     * @param type Le type de conflit
     */
    public void setType(TypeConflit type) {
        this.type = type;
    }
    
    /**
     * Retourne la durée du chevauchement en minutes.
     * 
     * @return La durée en minutes
     */
    public long getDureeChevauchementsMinutes() {
        return dureeChevauchementsMinutes;
    }
    
    /**
     * Définit la durée du chevauchement.
     * 
     * @param dureeChevauchementsMinutes La durée en minutes
     */
    public void setDureeChevauchementsMinutes(long dureeChevauchementsMinutes) {
        this.dureeChevauchementsMinutes = dureeChevauchementsMinutes;
    }
    
    /**
     * Retourne la description du conflit.
     * 
     * @return La description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Définit la description du conflit.
     * 
     * @param description La nouvelle description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Retourne l'ID du premier cours impliqué.
     * 
     * @return L'ID du premier cours, ou null si horaire1 est null
     */
    public String getCours1Id() {
        return horaire1 != null ? horaire1.getCourseId() : null;
    }
    
    /**
     * Retourne l'ID du second cours impliqué.
     * 
     * @return L'ID du second cours, ou null si horaire2 est null
     */
    public String getCours2Id() {
        return horaire2 != null ? horaire2.getCourseId() : null;
    }
    
    /**
     * Retourne une représentation textuelle du conflit.
     * 
     * @return Une chaîne décrivant le conflit
     */
    @Override
    public String toString() {
        return "ConflitHoraire{" +
                "type=" + type +
                ", cours1=" + (horaire1 != null ? horaire1.getCourseId() : "null") +
                ", cours2=" + (horaire2 != null ? horaire2.getCourseId() : "null") +
                ", duree=" + dureeChevauchementsMinutes + " min" +
                '}';
    }
    
    /**
     * Vérifie l'égalité avec un autre objet.
     * 
     * @param o L'objet à comparer
     * @return {@code true} si les conflits sont égaux
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConflitHoraire that = (ConflitHoraire) o;
        return Objects.equals(horaire1, that.horaire1) && 
               Objects.equals(horaire2, that.horaire2);
    }
    
    /**
     * Calcule le code de hachage.
     * 
     * @return Le code de hachage
     */
    @Override
    public int hashCode() {
        return Objects.hash(horaire1, horaire2);
    }
}
