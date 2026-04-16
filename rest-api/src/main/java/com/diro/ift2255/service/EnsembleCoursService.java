package com.diro.ift2255.service;

import com.diro.ift2255.model.Course;
import com.diro.ift2255.model.EnsembleCours;
import com.diro.ift2255.model.Horaire;
import com.diro.ift2255.repository.EnsembleCoursRepository;
import com.diro.ift2255.util.HttpClientApi;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Service pour la gestion des ensembles de cours (CU#10).
 * Implémente la logique métier pour créer, modifier et consulter des ensembles.
 * 
 * <p>Ce service communique avec l'API Planifium pour récupérer les horaires
 * des cours et les associer aux ensembles créés par les utilisateurs.</p>
 * 
 * <h2>Fonctionnalités principales :</h2>
 * <ul>
 *   <li>Création d'ensembles avec validation (max 6 cours)</li>
 *   <li>Ajout/retrait de cours d'un ensemble</li>
 *   <li>Récupération des horaires combinés</li>
 *   <li>Validation du format de trimestre</li>
 * </ul>
 * 
 * @author Équipe 10
 * @version 3.0
 * @since 2025-12-27
 * @see EnsembleCours
 * @see EnsembleCoursRepository
 */
public class EnsembleCoursService {
    
    /**
     * URL de base de l'API Planifium.
     */
    private static final String PLANIFIUM_BASE_URL = "https://planifium-api.onrender.com/api/v1";
    
    /**
     * Pattern de validation du format de trimestre.
     * Accepte: A25, H25, E25 (Automne, Hiver, Été + année sur 2 chiffres).
     */
    private static final Pattern TRIMESTRE_PATTERN = Pattern.compile("^[AHE]\\d{2}$", Pattern.CASE_INSENSITIVE);
    
    /**
     * Repository pour la persistance des ensembles.
     */
    private final EnsembleCoursRepository repository;
    
    /**
     * Client HTTP pour les appels à l'API Planifium.
     */
    private final HttpClientApi httpClient;
    
    /**
     * Constructeur avec injection de dépendances.
     * 
     * @param repository Le repository des ensembles
     * @param httpClient Le client HTTP pour l'API Planifium
     */
    public EnsembleCoursService(EnsembleCoursRepository repository, HttpClientApi httpClient) {
        this.repository = repository;
        this.httpClient = httpClient;
    }
    
    /**
     * Crée un nouvel ensemble de cours.
     * Valide les paramètres et charge les horaires depuis Planifium.
     * 
     * @param nom Le nom descriptif de l'ensemble
     * @param trimestre Le code du trimestre (format: A25, H25, E25)
     * @param coursIds La liste des IDs de cours (max 6, dédupliqués)
     * @return L'ensemble créé avec son ID généré
     * @throws IllegalArgumentException si les paramètres sont invalides
     */
    public EnsembleCours creerEnsemble(String nom, String trimestre, List<String> coursIds) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'ensemble est requis");
        }
        
        String normalizedTrimestre = trimestre != null ? trimestre.toUpperCase() : null;
        if (!isTrimestreValide(normalizedTrimestre)) {
            throw new IllegalArgumentException("Format de trimestre invalide. Utilisez A25, H25 ou E25.");
        }
        
        if (coursIds == null || coursIds.isEmpty()) {
            throw new IllegalArgumentException("Au moins un cours est requis");
        }
        
        List<String> normalizedIds = new ArrayList<>();
        for (String id : coursIds) {
            String normalized = id.toUpperCase();
            if (!normalizedIds.contains(normalized)) {
                normalizedIds.add(normalized);
            }
        }
        
        if (normalizedIds.size() > EnsembleCours.MAX_COURS) {
            throw new IllegalArgumentException(
                    "Un ensemble ne peut contenir plus de " + EnsembleCours.MAX_COURS + " cours. " +
                    "Vous avez fourni " + normalizedIds.size() + " cours.");
        }
        
        EnsembleCours ensemble = new EnsembleCours();
        ensemble.setNom(nom);
        ensemble.setTrimestre(normalizedTrimestre);
        ensemble.setCoursIds(normalizedIds);
        
        chargerHoraires(ensemble);
        
        return repository.save(ensemble);
    }
    
    /**
     * Ajoute un cours à un ensemble existant.
     * 
     * @param ensembleId L'ID de l'ensemble
     * @param courseId L'ID du cours à ajouter
     * @return L'ensemble mis à jour
     * @throws IllegalArgumentException si l'ensemble n'existe pas, est plein,
     *         ou si le cours est déjà présent
     */
    public EnsembleCours ajouterCours(String ensembleId, String courseId) {
        EnsembleCours ensemble = repository.findById(ensembleId)
                .orElseThrow(() -> new IllegalArgumentException("Ensemble non trouvé: " + ensembleId));
        
        if (ensemble.estPlein()) {
            throw new IllegalArgumentException("L'ensemble a atteint sa capacité maximale de " + 
                    EnsembleCours.MAX_COURS + " cours");
        }
        
        String normalizedId = courseId.toUpperCase();
        if (ensemble.getCoursIds().contains(normalizedId)) {
            throw new IllegalArgumentException("Le cours " + normalizedId + " est déjà dans l'ensemble");
        }
        
        ensemble.ajouterCours(normalizedId);
        chargerHoraireCours(ensemble, normalizedId);
        
        return repository.update(ensemble);
    }
    
    /**
     * Retire un cours d'un ensemble.
     * 
     * @param ensembleId L'ID de l'ensemble
     * @param courseId L'ID du cours à retirer
     * @return L'ensemble mis à jour
     * @throws IllegalArgumentException si l'ensemble n'existe pas ou
     *         si le cours n'est pas dans l'ensemble
     */
    public EnsembleCours retirerCours(String ensembleId, String courseId) {
        EnsembleCours ensemble = repository.findById(ensembleId)
                .orElseThrow(() -> new IllegalArgumentException("Ensemble non trouvé: " + ensembleId));
        
        String normalizedId = courseId.toUpperCase();
        if (!ensemble.getCoursIds().contains(normalizedId)) {
            throw new IllegalArgumentException("Le cours " + normalizedId + " n'est pas dans l'ensemble");
        }
        
        ensemble.retirerCours(normalizedId);
        ensemble.getHoraires().remove(normalizedId);
        
        return repository.update(ensemble);
    }
    
    /**
     * Met à jour un ensemble existant.
     * Seuls les paramètres non-null sont mis à jour.
     * 
     * @param id L'ID de l'ensemble à modifier
     * @param nom Le nouveau nom (null pour ne pas modifier)
     * @param trimestre Le nouveau trimestre (null pour ne pas modifier)
     * @param coursIds La nouvelle liste de cours (null pour ne pas modifier)
     * @return L'ensemble mis à jour
     * @throws IllegalArgumentException si l'ensemble n'existe pas ou
     *         si les nouvelles valeurs sont invalides
     */
    public EnsembleCours updateEnsemble(String id, String nom, String trimestre, List<String> coursIds) {
        EnsembleCours ensemble = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ensemble non trouvé: " + id));
        
        if (nom != null && !nom.trim().isEmpty()) {
            ensemble.setNom(nom);
        }
        
        if (trimestre != null) {
            String normalizedTrimestre = trimestre.toUpperCase();
            if (!isTrimestreValide(normalizedTrimestre)) {
                throw new IllegalArgumentException("Format de trimestre invalide");
            }
            ensemble.setTrimestre(normalizedTrimestre);
        }
        
        if (coursIds != null) {
            if (coursIds.size() > EnsembleCours.MAX_COURS) {
                throw new IllegalArgumentException("Maximum " + EnsembleCours.MAX_COURS + " cours autorisés");
            }
            
            List<String> normalizedIds = new ArrayList<>();
            for (String courseId : coursIds) {
                normalizedIds.add(courseId.toUpperCase());
            }
            ensemble.setCoursIds(normalizedIds);
            chargerHoraires(ensemble);
        }
        
        return repository.update(ensemble);
    }
    
    /**
     * Récupère l'horaire combiné de tous les cours d'un ensemble.
     * 
     * @param ensembleId L'ID de l'ensemble
     * @return Map associant chaque cours à ses horaires
     * @throws IllegalArgumentException si l'ensemble n'existe pas
     */
    public Map<String, List<Horaire>> getHoraireCombine(String ensembleId) {
        EnsembleCours ensemble = repository.findById(ensembleId)
                .orElseThrow(() -> new IllegalArgumentException("Ensemble non trouvé: " + ensembleId));
        
        return ensemble.getHoraires();
    }
    
    /**
     * Récupère un ensemble par son ID.
     * 
     * @param id L'ID de l'ensemble
     * @return Un {@link Optional} contenant l'ensemble si trouvé
     */
    public Optional<EnsembleCours> getEnsembleById(String id) {
        return repository.findById(id);
    }
    
    /**
     * Récupère tous les ensembles.
     * 
     * @return Liste de tous les ensembles
     */
    public List<EnsembleCours> getAllEnsembles() {
        return repository.findAll();
    }
    
    /**
     * Récupère les ensembles pour un trimestre donné.
     * 
     * @param trimestre Le code du trimestre
     * @return Liste des ensembles correspondants
     */
    public List<EnsembleCours> getEnsemblesByTrimestre(String trimestre) {
        return repository.findByTrimestre(trimestre);
    }
    
    /**
     * Supprime un ensemble.
     * 
     * @param id L'ID de l'ensemble à supprimer
     * @return {@code true} si l'ensemble a été supprimé
     */
    public boolean deleteEnsemble(String id) {
        return repository.deleteById(id);
    }
    
    /**
     * Charge les horaires de tous les cours d'un ensemble depuis Planifium.
     * 
     * @param ensemble L'ensemble dont les horaires doivent être chargés
     */
    private void chargerHoraires(EnsembleCours ensemble) {
        Map<String, List<Horaire>> horaires = new HashMap<>();
        
        for (String courseId : ensemble.getCoursIds()) {
            List<Horaire> coursHoraires = chargerHoraireDepuisApi(courseId, ensemble.getTrimestre());
            horaires.put(courseId, coursHoraires);
        }
        
        ensemble.setHoraires(horaires);
    }
    
    /**
     * Charge l'horaire d'un seul cours et l'ajoute à l'ensemble.
     * 
     * @param ensemble L'ensemble cible
     * @param courseId L'ID du cours
     */
    private void chargerHoraireCours(EnsembleCours ensemble, String courseId) {
        List<Horaire> coursHoraires = chargerHoraireDepuisApi(courseId, ensemble.getTrimestre());
        ensemble.getHoraires().put(courseId, coursHoraires);
    }
    
    /**
     * Charge l'horaire d'un cours depuis l'API Planifium.
     * 
     * @param courseId L'ID du cours
     * @param trimestre Le code du trimestre
     * @return Liste des horaires (vide en cas d'erreur)
     */
    private List<Horaire> chargerHoraireDepuisApi(String courseId, String trimestre) {
        List<Horaire> horaires = new ArrayList<>();
        
        try {
            String url = PLANIFIUM_BASE_URL + "/courses/" + courseId.toLowerCase() + 
                    "?include_schedule=true&schedule_semester=" + trimestre.toLowerCase();
            
            JsonNode response = httpClient.get(URI.create(url), JsonNode.class);
            
            if (response != null && response.has("schedule")) {
                JsonNode scheduleNode = response.get("schedule");
                horaires.addAll(parseSchedule(courseId, scheduleNode));
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'horaire pour " + courseId + ": " + e.getMessage());
        }
        
        return horaires;
    }
    
    /**
     * Parse le JSON de l'horaire en objets {@link Horaire}.
     * 
     * @param courseId L'ID du cours
     * @param scheduleNode Le noeud JSON contenant l'horaire
     * @return Liste des horaires parsés
     */
    private List<Horaire> parseSchedule(String courseId, JsonNode scheduleNode) {
        List<Horaire> horaires = new ArrayList<>();
        
        if (scheduleNode == null || !scheduleNode.isArray()) {
            return horaires;
        }
        
        for (JsonNode section : scheduleNode) {
            if (section.has("activities")) {
                for (JsonNode activity : section.get("activities")) {
                    if (activity.has("schedules")) {
                        for (JsonNode schedule : activity.get("schedules")) {
                            try {
                                Horaire horaire = parseHoraireNode(courseId, schedule);
                                if (horaire != null) {
                                    horaires.add(horaire);
                                }
                            } catch (Exception e) {
                                // Skip invalid entries
                            }
                        }
                    }
                }
            }
        }
        
        return horaires;
    }
    
    /**
     * Parse un noeud JSON en objet {@link Horaire}.
     * 
     * @param courseId L'ID du cours
     * @param node Le noeud JSON
     * @return L'horaire parsé, ou null si invalide
     */
    private Horaire parseHoraireNode(String courseId, JsonNode node) {
        if (!node.has("day") || !node.has("start_time") || !node.has("end_time")) {
            return null;
        }
        
        String day = node.get("day").asText();
        String startTime = node.get("start_time").asText();
        String endTime = node.get("end_time").asText();
        String local = node.has("room") ? node.get("room").asText() : "";
        
        DayOfWeek dayOfWeek = parseDayOfWeek(day);
        if (dayOfWeek == null) {
            return null;
        }
        
        return new Horaire(courseId, dayOfWeek, LocalTime.parse(startTime), 
                LocalTime.parse(endTime), local, null, null);
    }
    
    /**
     * Parse le nom du jour vers {@link DayOfWeek}.
     * 
     * @param day Le nom du jour (français ou anglais)
     * @return Le {@link DayOfWeek} correspondant, ou null si non reconnu
     */
    private DayOfWeek parseDayOfWeek(String day) {
        if (day == null) return null;
        
        switch (day.toLowerCase()) {
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
            default:
                return null;
        }
    }
    
    /**
     * Valide le format du code de trimestre.
     * 
     * @param trimestre Le code à valider
     * @return {@code true} si le format est valide
     */
    private boolean isTrimestreValide(String trimestre) {
        return trimestre != null && TRIMESTRE_PATTERN.matcher(trimestre).matches();
    }
}
