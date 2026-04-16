package com.diro.ift2255.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Représente un créneau horaire pour un cours.
 * Contient le jour, les heures de début et de fin, le local et le type d'activité.
 * 
 * <p>Cette classe fournit également des méthodes pour détecter les chevauchements
 * entre créneaux horaires, ce qui est essentiel pour la détection des conflits.</p>
 * 
 * @author Équipe 10
 * @version 3.0
 * @since 2025-12-27
 * @see ConflitHoraire
 * @see EnsembleCours
 */
public class Horaire {
    
    /**
     * Identifiant du cours auquel appartient ce créneau.
     */
    private String courseId;
    
    /**
     * Jour de la semaine du créneau.
     */
    private DayOfWeek jour;
    
    /**
     * Heure de début du créneau.
     */
    private LocalTime heureDebut;
    
    /**
     * Heure de fin du créneau.
     */
    private LocalTime heureFin;
    
    /**
     * Numéro ou nom du local où se déroule l'activité.
     */
    private String local;
    
    /**
     * Type d'activité (Cours magistral, TP, Lab, etc.).
     */
    private String typeActivite;
    
    /**
     * Identifiant de la section du cours.
     */
    private String section;
    
    /**
     * Formateur pour parser les heures au format "HH:mm".
     */
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    
    /**
     * Constructeur par défaut.
     */
    public Horaire() {
    }
    
    /**
     * Constructeur avec les paramètres essentiels.
     * 
     * @param courseId L'identifiant du cours
     * @param jour Le jour de la semaine
     * @param heureDebut L'heure de début
     * @param heureFin L'heure de fin
     */
    public Horaire(String courseId, DayOfWeek jour, LocalTime heureDebut, LocalTime heureFin) {
        this.courseId = courseId;
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }
    
    /**
     * Constructeur avec jour et heures en format String.
     * 
     * @param courseId L'identifiant du cours
     * @param jour Le jour de la semaine (ex: "Lundi", "Monday")
     * @param heureDebut L'heure de début au format "HH:mm"
     * @param heureFin L'heure de fin au format "HH:mm"
     * @param local Le numéro du local
     */
    public Horaire(String courseId, String jour, String heureDebut, String heureFin, String local) {
        this.courseId = courseId;
        this.jour = parseJour(jour);
        this.heureDebut = LocalTime.parse(heureDebut, TIME_FORMAT);
        this.heureFin = LocalTime.parse(heureFin, TIME_FORMAT);
        this.local = local;
    }
    
    /**
     * Constructeur complet avec tous les paramètres.
     * 
     * @param courseId L'identifiant du cours
     * @param jour Le jour de la semaine
     * @param heureDebut L'heure de début
     * @param heureFin L'heure de fin
     * @param local Le numéro du local
     * @param typeActivite Le type d'activité
     * @param section L'identifiant de la section
     */
    public Horaire(String courseId, DayOfWeek jour, LocalTime heureDebut, LocalTime heureFin, 
                   String local, String typeActivite, String section) {
        this.courseId = courseId;
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.local = local;
        this.typeActivite = typeActivite;
        this.section = section;
    }
    
    /**
     * Parse un nom de jour en français ou anglais vers {@link DayOfWeek}.
     * 
     * @param jour Le nom du jour (ex: "lundi", "Monday")
     * @return Le {@link DayOfWeek} correspondant
     * @throws IllegalArgumentException si le jour n'est pas reconnu
     */
    private DayOfWeek parseJour(String jour) {
        if (jour == null) return null;
        
        switch (jour.toLowerCase().trim()) {
            case "lundi":
            case "monday":
                return DayOfWeek.MONDAY;
            case "mardi":
            case "tuesday":
                return DayOfWeek.TUESDAY;
            case "mercredi":
            case "wednesday":
                return DayOfWeek.WEDNESDAY;
            case "jeudi":
            case "thursday":
                return DayOfWeek.THURSDAY;
            case "vendredi":
            case "friday":
                return DayOfWeek.FRIDAY;
            case "samedi":
            case "saturday":
                return DayOfWeek.SATURDAY;
            case "dimanche":
            case "sunday":
                return DayOfWeek.SUNDAY;
            default:
                throw new IllegalArgumentException("Jour invalide: " + jour);
        }
    }
    
    /**
     * Vérifie si cet horaire chevauche un autre horaire.
     * Deux horaires se chevauchent s'ils sont le même jour et que leurs
     * plages horaires s'intersectent.
     * 
     * @param autre L'autre horaire à comparer
     * @return {@code true} si les horaires se chevauchent, {@code false} sinon
     */
    public boolean chevauche(Horaire autre) {
        if (autre == null) return false;
        
        if (this.jour != autre.jour) {
            return false;
        }
        
        return this.heureDebut.isBefore(autre.heureFin) && 
               autre.heureDebut.isBefore(this.heureFin);
    }
    
    /**
     * Calcule la durée du chevauchement avec un autre horaire en minutes.
     * 
     * @param autre L'autre horaire
     * @return La durée du chevauchement en minutes, 0 si pas de chevauchement
     */
    public long getDureeChevauchementsMinutes(Horaire autre) {
        if (!chevauche(autre)) {
            return 0;
        }
        
        LocalTime debutChevauchement = this.heureDebut.isAfter(autre.heureDebut) ? 
                this.heureDebut : autre.heureDebut;
        LocalTime finChevauchement = this.heureFin.isBefore(autre.heureFin) ? 
                this.heureFin : autre.heureFin;
        
        return java.time.Duration.between(debutChevauchement, finChevauchement).toMinutes();
    }
    
    /**
     * Vérifie si le chevauchement est total (horaires identiques).
     * 
     * @param autre L'autre horaire
     * @return {@code true} si les horaires ont les mêmes heures de début et fin
     */
    public boolean estChevauchementTotal(Horaire autre) {
        if (!chevauche(autre)) return false;
        
        return this.heureDebut.equals(autre.heureDebut) && 
               this.heureFin.equals(autre.heureFin);
    }
    
    /**
     * Retourne l'identifiant du cours.
     * 
     * @return L'ID du cours
     */
    public String getCourseId() {
        return courseId;
    }
    
    /**
     * Définit l'identifiant du cours.
     * 
     * @param courseId Le nouvel ID du cours
     */
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
    
    /**
     * Retourne le jour de la semaine.
     * 
     * @return Le jour de la semaine
     */
    public DayOfWeek getJour() {
        return jour;
    }
    
    /**
     * Définit le jour de la semaine.
     * 
     * @param jour Le nouveau jour
     */
    public void setJour(DayOfWeek jour) {
        this.jour = jour;
    }
    
    /**
     * Définit le jour de la semaine à partir d'une chaîne.
     * 
     * @param jour Le nom du jour (français ou anglais)
     */
    public void setJour(String jour) {
        this.jour = parseJour(jour);
    }
    
    /**
     * Retourne l'heure de début.
     * 
     * @return L'heure de début
     */
    public LocalTime getHeureDebut() {
        return heureDebut;
    }
    
    /**
     * Définit l'heure de début.
     * 
     * @param heureDebut La nouvelle heure de début
     */
    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }
    
    /**
     * Définit l'heure de début à partir d'une chaîne.
     * 
     * @param heureDebut L'heure au format "HH:mm"
     */
    public void setHeureDebut(String heureDebut) {
        this.heureDebut = LocalTime.parse(heureDebut, TIME_FORMAT);
    }
    
    /**
     * Retourne l'heure de fin.
     * 
     * @return L'heure de fin
     */
    public LocalTime getHeureFin() {
        return heureFin;
    }
    
    /**
     * Définit l'heure de fin.
     * 
     * @param heureFin La nouvelle heure de fin
     */
    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }
    
    /**
     * Définit l'heure de fin à partir d'une chaîne.
     * 
     * @param heureFin L'heure au format "HH:mm"
     */
    public void setHeureFin(String heureFin) {
        this.heureFin = LocalTime.parse(heureFin, TIME_FORMAT);
    }
    
    /**
     * Retourne le numéro du local.
     * 
     * @return Le local
     */
    public String getLocal() {
        return local;
    }
    
    /**
     * Définit le numéro du local.
     * 
     * @param local Le nouveau local
     */
    public void setLocal(String local) {
        this.local = local;
    }
    
    /**
     * Retourne le type d'activité.
     * 
     * @return Le type d'activité
     */
    public String getTypeActivite() {
        return typeActivite;
    }
    
    /**
     * Définit le type d'activité.
     * 
     * @param typeActivite Le nouveau type d'activité
     */
    public void setTypeActivite(String typeActivite) {
        this.typeActivite = typeActivite;
    }
    
    /**
     * Retourne l'identifiant de la section.
     * 
     * @return La section
     */
    public String getSection() {
        return section;
    }
    
    /**
     * Définit l'identifiant de la section.
     * 
     * @param section La nouvelle section
     */
    public void setSection(String section) {
        this.section = section;
    }
    
    /**
     * Retourne le nom du jour en français.
     * 
     * @return Le nom du jour (ex: "Lundi", "Mardi")
     */
    public String getJourString() {
        if (jour == null) return null;
        
        switch (jour) {
            case MONDAY: return "Lundi";
            case TUESDAY: return "Mardi";
            case WEDNESDAY: return "Mercredi";
            case THURSDAY: return "Jeudi";
            case FRIDAY: return "Vendredi";
            case SATURDAY: return "Samedi";
            case SUNDAY: return "Dimanche";
            default: return jour.toString();
        }
    }
    
    /**
     * Retourne une représentation textuelle de l'horaire.
     * 
     * @return Une chaîne décrivant l'horaire
     */
    @Override
    public String toString() {
        return "Horaire{" +
                "courseId='" + courseId + '\'' +
                ", jour=" + getJourString() +
                ", heureDebut=" + heureDebut +
                ", heureFin=" + heureFin +
                ", local='" + local + '\'' +
                '}';
    }
    
    /**
     * Vérifie l'égalité avec un autre objet.
     * 
     * @param o L'objet à comparer
     * @return {@code true} si les horaires sont égaux
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Horaire horaire = (Horaire) o;
        return Objects.equals(courseId, horaire.courseId) &&
               jour == horaire.jour &&
               Objects.equals(heureDebut, horaire.heureDebut) &&
               Objects.equals(heureFin, horaire.heureFin);
    }
    
    /**
     * Calcule le code de hachage.
     * 
     * @return Le code de hachage
     */
    @Override
    public int hashCode() {
        return Objects.hash(courseId, jour, heureDebut, heureFin);
    }
}
