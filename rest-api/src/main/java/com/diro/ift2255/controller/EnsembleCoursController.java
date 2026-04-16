package com.diro.ift2255.controller;

import com.diro.ift2255.model.EnsembleCours;
import com.diro.ift2255.model.Horaire;
import com.diro.ift2255.service.EnsembleCoursService;
import io.javalin.http.Context;

import java.util.*;

/**
 * Contrôleur REST pour la gestion des ensembles de cours (CU#10).
 * Gère les endpoints CRUD pour les ensembles de cours.
 * 
 * <p>Ce contrôleur expose les endpoints HTTP suivants :</p>
 * <ul>
 *   <li>POST /ensembles - Créer un nouvel ensemble</li>
 *   <li>GET /ensembles - Lister tous les ensembles</li>
 *   <li>GET /ensembles/{id} - Récupérer un ensemble par ID</li>
 *   <li>GET /ensembles/{id}/horaire - Récupérer l'horaire combiné</li>
 *   <li>PUT /ensembles/{id} - Mettre à jour un ensemble</li>
 *   <li>POST /ensembles/{id}/cours - Ajouter un cours</li>
 *   <li>DELETE /ensembles/{id}/cours/{courseId} - Retirer un cours</li>
 *   <li>DELETE /ensembles/{id} - Supprimer un ensemble</li>
 * </ul>
 * 
 * @author Notre équipe, l'équipe 10
 * @version 3.0
 * @since 2025-12-27
 * @see EnsembleCoursService
 * @see EnsembleCours
 */
public class EnsembleCoursController {

    /**
     * Service de gestion des ensembles de cours.
     * Injecté via le constructeur pour permettre les tests unitaires.
     */
    private final EnsembleCoursService ensembleService;

    /**
     * Constructeur avec injection de dépendance.
     * 
     * @param ensembleService Le service de gestion des ensembles
     */
    public EnsembleCoursController(EnsembleCoursService ensembleService) {
        this.ensembleService = ensembleService;
    }

    /**
     * Crée un nouvel ensemble de cours.
     * 
     * <p>Endpoint : POST /ensembles</p>
     * <p>Body JSON requis :</p>
     * <pre>
     * {
     *   "nom": "Mon ensemble",
     *   "trimestre": "A25",
     *   "coursIds": ["IFT1015", "IFT2255"]
     * }
     * </pre>
     * 
     * @param ctx Le contexte Javalin contenant la requête HTTP
     */
    public void creerEnsemble(Context ctx) {
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            
            String nom = (String) body.get("nom");
            String trimestre = (String) body.get("trimestre");
            
            @SuppressWarnings("unchecked")
            List<String> coursIds = (List<String>) body.get("coursIds");

            if (nom == null || nom.trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "Le nom de l'ensemble est requis"));
                return;
            }

            if (trimestre == null || trimestre.trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "Le trimestre est requis"));
                return;
            }

            if (coursIds == null || coursIds.isEmpty()) {
                ctx.status(400).json(Map.of("error", "Au moins un cours est requis"));
                return;
            }

            EnsembleCours ensemble = ensembleService.creerEnsemble(nom, trimestre, coursIds);
            ctx.status(201).json(ensemble);
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }

    /**
     * Récupère tous les ensembles ou filtre par trimestre.
     * 
     * <p>Endpoint : GET /ensembles</p>
     * <p>Paramètre optionnel : ?trimestre=A25</p>
     * 
     * @param ctx Le contexte Javalin contenant la requête HTTP
     */
    public void getAllEnsembles(Context ctx) {
        try {
            String trimestre = ctx.queryParam("trimestre");
            
            List<EnsembleCours> ensembles;
            if (trimestre != null && !trimestre.trim().isEmpty()) {
                ensembles = ensembleService.getEnsemblesByTrimestre(trimestre);
            } else {
                ensembles = ensembleService.getAllEnsembles();
            }
            
            ctx.json(Map.of(
                "ensembles", ensembles,
                "count", ensembles.size()
            ));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }

    /**
     * Récupère un ensemble par son identifiant.
     * 
     * <p>Endpoint : GET /ensembles/{id}</p>
     * 
     * @param ctx Le contexte Javalin contenant la requête HTTP
     */
    public void getEnsembleById(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            
            if (id == null || id.trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "L'ID de l'ensemble est requis"));
                return;
            }

            Optional<EnsembleCours> ensemble = ensembleService.getEnsembleById(id);
            
            if (ensemble.isPresent()) {
                ctx.json(ensemble.get());
            } else {
                ctx.status(404).json(Map.of("error", "Ensemble non trouvé avec l'ID: " + id));
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }

    /**
     * Récupère l'horaire combiné de tous les cours d'un ensemble.
     * 
     * <p>Endpoint : GET /ensembles/{id}/horaire</p>
     * 
     * @param ctx Le contexte Javalin contenant la requête HTTP
     */
    public void getHoraireCombine(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            
            if (id == null || id.trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "L'ID de l'ensemble est requis"));
                return;
            }

            Map<String, List<Horaire>> horaires = ensembleService.getHoraireCombine(id);
            ctx.json(Map.of(
                "ensembleId", id,
                "horaires", horaires
            ));
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }

    /**
     * Met à jour un ensemble existant.
     * 
     * <p>Endpoint : PUT /ensembles/{id}</p>
     * <p>Body JSON (tous les champs sont optionnels) :</p>
     * <pre>
     * {
     *   "nom": "Nouveau nom",
     *   "trimestre": "H25",
     *   "coursIds": ["IFT1015", "IFT2255", "IFT2015"]
     * }
     * </pre>
     * 
     * @param ctx Le contexte Javalin contenant la requête HTTP
     */
    public void updateEnsemble(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            
            if (id == null || id.trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "L'ID de l'ensemble est requis"));
                return;
            }

            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            
            String nom = (String) body.get("nom");
            String trimestre = (String) body.get("trimestre");
            
            @SuppressWarnings("unchecked")
            List<String> coursIds = (List<String>) body.get("coursIds");

            EnsembleCours ensemble = ensembleService.updateEnsemble(id, nom, trimestre, coursIds);
            ctx.json(ensemble);
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }

    /**
     * Ajoute un cours à un ensemble existant.
     * 
     * <p>Endpoint : POST /ensembles/{id}/cours</p>
     * <p>Body JSON requis :</p>
     * <pre>
     * {
     *   "courseId": "IFT2015"
     * }
     * </pre>
     * 
     * @param ctx Le contexte Javalin contenant la requête HTTP
     */
    public void ajouterCours(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            
            if (id == null || id.trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "L'ID de l'ensemble est requis"));
                return;
            }

            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            String courseId = (String) body.get("courseId");

            if (courseId == null || courseId.trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "L'ID du cours est requis"));
                return;
            }

            EnsembleCours ensemble = ensembleService.ajouterCours(id, courseId);
            ctx.json(ensemble);
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }

    /**
     * Retire un cours d'un ensemble.
     * 
     * <p>Endpoint : DELETE /ensembles/{id}/cours/{courseId}</p>
     * 
     * @param ctx Le contexte Javalin contenant la requête HTTP
     */
    public void retirerCours(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            String courseId = ctx.pathParam("courseId");
            
            if (id == null || id.trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "L'ID de l'ensemble est requis"));
                return;
            }

            if (courseId == null || courseId.trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "L'ID du cours est requis"));
                return;
            }

            EnsembleCours ensemble = ensembleService.retirerCours(id, courseId);
            ctx.json(ensemble);
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }

    /**
     * Supprime un ensemble.
     * 
     * <p>Endpoint : DELETE /ensembles/{id}</p>
     * 
     * @param ctx Le contexte Javalin contenant la requête HTTP
     */
    public void deleteEnsemble(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            
            if (id == null || id.trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "L'ID de l'ensemble est requis"));
                return;
            }

            boolean deleted = ensembleService.deleteEnsemble(id);
            
            if (deleted) {
                ctx.json(Map.of(
                    "message", "Ensemble supprimé avec succès",
                    "id", id
                ));
            } else {
                ctx.status(404).json(Map.of("error", "Ensemble non trouvé avec l'ID: " + id));
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }
}
