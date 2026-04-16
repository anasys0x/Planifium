package com.diro.ift2255.model;

import java.util.*;

/**
 * Représente un ensemble de cours pour un trimestre donné.
 * Un ensemble peut contenir au maximum {@value #MAX_COURS} cours.
 * 
 * <p>Cette classe est utilisée pour regrouper plusieurs cours
 * qu'un étudiant souhaite suivre durant un même trimestre,
 * permettant ainsi de visualiser l'horaire combiné et de
 * détecter les conflits potentiels.</p>
 * 
 * @author Équipe 10
 * @version 3.0
 * @since 2025-12-27
 * @see Course
 * @see Horaire
 */
public class EnsembleCours {
    
    /**
     * Nombre maximum de cours autorisés dans un ensemble.
     * Cette limite est fixée à 6 cours conformément aux spécifications.
     */
    public static final int MAX_COURS = 6;
    
    /**
     * Identifiant unique de l'ensemble.
     * Format: "ENS-XXXXX" où X est un chiffre.
     */
    private String id;
    
    /**
     * Nom descriptif de l'ensemble choisi par l'utilisateur.
     */
    private String nom;
    
    /**
     * Code du trimestre pour lequel l'ensemble est créé.
     * Format: [A|H|E][0-9]{2} (ex: "A25" pour Automne 2025).
     */
    private String trimestre;
    
    /**
     * Liste des identifiants de cours inclus dans l'ensemble.
     * Les IDs sont normalisés en majuscules.
     */
    private List<String> coursIds;
    
    /**
     * Liste des objets Course avec les détails complets des cours.
     */
    private List<Course> coursDetails;
    
    /**
     * Map associant chaque ID de cours à sa liste d'horaires.
     * Clé: ID du cours, Valeur: Liste des créneaux horaires.
     */
    private Map<String, List<Horaire>> horaires;
    
    /**
     * Date de création de l'ensemble.
     */
    private Date dateCreation;
    
    /**
     * Constructeur par défaut.
     * Initialise les collections vides et la date de création.
     */
    public EnsembleCours() {
        this.coursIds = new ArrayList<>();
        this.coursDetails = new ArrayList<>();
        this.horaires = new HashMap<>();
        this.dateCreation = new Date();
    }
    
    /**
     * Constructeur avec id, nom et trimestre.
     * 
     * @param id L'identifiant unique de l'ensemble
     * @param nom Le nom descriptif de l'ensemble
     * @param trimestre Le code du trimestre (ex: "A25", "H25", "E25")
     */
    public EnsembleCours(String id, String nom, String trimestre) {
        this();
        this.id = id;
        this.nom = nom;
        this.trimestre = trimestre;
    }
    
    /**
     * Constructeur complet avec liste de cours.
     * 
     * @param id L'identifiant unique de l'ensemble
     * @param nom Le nom descriptif de l'ensemble
     * @param trimestre Le code du trimestre
     * @param coursIds La liste des identifiants de cours à inclure
     * @throws IllegalArgumentException si le nombre de cours dépasse {@value #MAX_COURS}
     */
    public EnsembleCours(String id, String nom, String trimestre, List<String> coursIds) {
        this(id, nom, trimestre);
        if (coursIds != null && coursIds.size() <= MAX_COURS) {
            this.coursIds = new ArrayList<>(coursIds);
        } else if (coursIds != null) {
            throw new IllegalArgumentException("Un ensemble ne peut contenir plus de " + MAX_COURS + " cours.");
        }
    }
    
    /**
     * Retourne l'identifiant unique de l'ensemble.
     * 
     * @return L'identifiant de l'ensemble
     */
    public String getId() {
        return id;
    }
    
    /**
     * Définit l'identifiant unique de l'ensemble.
     * 
     * @param id Le nouvel identifiant
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Retourne le nom descriptif de l'ensemble.
     * 
     * @return Le nom de l'ensemble
     */
    public String getNom() {
        return nom;
    }
    
    /**
     * Définit le nom descriptif de l'ensemble.
     * 
     * @param nom Le nouveau nom
     */
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    /**
     * Retourne le code du trimestre.
     * 
     * @return Le trimestre (ex: "A25")
     */
    public String getTrimestre() {
        return trimestre;
    }
    
    /**
     * Définit le code du trimestre.
     * 
     * @param trimestre Le nouveau code de trimestre
     */
    public void setTrimestre(String trimestre) {
        this.trimestre = trimestre;
    }
    
    /**
     * Retourne la liste des identifiants de cours.
     * 
     * @return Liste des IDs de cours
     */
    public List<String> getCoursIds() {
        return coursIds;
    }
    
    /**
     * Définit la liste des identifiants de cours.
     * 
     * @param coursIds La nouvelle liste d'IDs
     */
    public void setCoursIds(List<String> coursIds) {
        this.coursIds = coursIds != null ? coursIds : new ArrayList<>();
    }
    
    /**
     * Retourne la liste des détails complets des cours.
     * 
     * @return Liste des objets Course
     */
    public List<Course> getCoursDetails() {
        return coursDetails;
    }
    
    /**
     * Définit la liste des détails des cours.
     * 
     * @param coursDetails La nouvelle liste de cours
     */
    public void setCoursDetails(List<Course> coursDetails) {
        this.coursDetails = coursDetails != null ? coursDetails : new ArrayList<>();
    }
    
    /**
     * Retourne la map des horaires par cours.
     * 
     * @return Map associant chaque cours à ses horaires
     */
    public Map<String, List<Horaire>> getHoraires() {
        return horaires;
    }
    
    /**
     * Définit la map des horaires.
     * 
     * @param horaires La nouvelle map des horaires
     */
    public void setHoraires(Map<String, List<Horaire>> horaires) {
        this.horaires = horaires != null ? horaires : new HashMap<>();
    }
    
    /**
     * Retourne la date de création de l'ensemble.
     * 
     * @return La date de création
     */
    public Date getDateCreation() {
        return dateCreation;
    }
    
    /**
     * Définit la date de création de l'ensemble.
     * 
     * @param dateCreation La nouvelle date de création
     */
    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    /**
     * Ajoute un cours à l'ensemble si la limite n'est pas atteinte.
     * L'ID du cours est normalisé en majuscules.
     * 
     * @param courseId L'identifiant du cours à ajouter
     * @return {@code true} si le cours a été ajouté, {@code false} si l'ensemble
     *         est plein ou si le cours est déjà présent
     */
    public boolean ajouterCours(String courseId) {
        if (coursIds.size() >= MAX_COURS) {
            return false;
        }
        String normalizedId = courseId.toUpperCase();
        if (coursIds.contains(normalizedId)) {
            return false;
        }
        return coursIds.add(normalizedId);
    }
    
    /**
     * Retire un cours de l'ensemble.
     * 
     * @param courseId L'identifiant du cours à retirer
     * @return {@code true} si le cours a été retiré, {@code false} sinon
     */
    public boolean retirerCours(String courseId) {
        return coursIds.remove(courseId.toUpperCase());
    }
    
    /**
     * Vérifie si l'ensemble a atteint sa capacité maximale.
     * 
     * @return {@code true} si l'ensemble contient {@value #MAX_COURS} cours
     */
    public boolean estPlein() {
        return coursIds.size() >= MAX_COURS;
    }
    
    /**
     * Retourne le nombre de cours actuellement dans l'ensemble.
     * 
     * @return Le nombre de cours
     */
    public int getNombreCours() {
        return coursIds.size();
    }
    
    /**
     * Calcule le nombre total de crédits de l'ensemble.
     * Gère le cas où {@link Course#getCredits()} retourne un String.
     * 
     * @return Le nombre total de crédits (défaut: 3 par cours si non parsable)
     */
    public int getTotalCredits() {
        return coursDetails.stream()
                .mapToInt(course -> {
                    try {
                        String creditsStr = course.getCredits();
                        if (creditsStr == null || creditsStr.isEmpty()) {
                            return 3;
                        }
                        return Integer.parseInt(creditsStr.trim());
                    } catch (NumberFormatException e) {
                        return 3;
                    }
                })
                .sum();
    }
    
    /**
     * Retourne une représentation textuelle de l'ensemble.
     * 
     * @return Une chaîne décrivant l'ensemble
     */
    @Override
    public String toString() {
        return "EnsembleCours{" +
                "id='" + id + '\'' +
                ", nom='" + nom + '\'' +
                ", trimestre='" + trimestre + '\'' +
                ", coursIds=" + coursIds +
                ", nombreCours=" + coursIds.size() +
                '}';
    }
}
