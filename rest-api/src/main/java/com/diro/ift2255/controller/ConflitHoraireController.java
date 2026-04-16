package com.diro.ift2255.controller;

import com.diro.ift2255.model.ConflitHoraire;
import com.diro.ift2255.model.Horaire;
import com.diro.ift2255.service.ConflitHoraireService;
import io.javalin.http.Context;

import java.time.DayOfWeek;
import java.util.*;

/**
 * Contrôleur REST pour la détection des conflits d'horaire (CU#11).
 * Gère les endpoints liés à la détection et l'analyse des conflits.
 * 
 * <p>Ce contrôleur expose les endpoints HTTP suivants :</p>
 * <ul>
 *   <li>GET /ensembles/{id}/conflits - Récupérer tous les conflits</li>
 *   <li>GET /ensembles/{id}/conflits/resume - Récupérer un résumé des conflits</li>
 *   <li>GET /ensembles/{id}/conflits/par-jour - Conflits groupés par jour</li>
 *   <li>GET /ensembles/{id}/conflits/suggestions - Suggestions de résolution</li>
 *   <li>POST /ensembles/{id}/conflits/verifier-ajout - Vérifier ajout d'un cours</li>
 *   <li>GET /ensembles/{id}/conflits/has-conflits - Vérification rapide</li>
 *   <li>DELETE /ensembles/{id}/conflits/cache - Invalider le cache</li>
 * </ul>
 * 
 * @author Notre équipe, l'équipe 10
 * @version 3.0
 * @since 2025-12-27
 * @see ConflitHoraireService
 * @see ConflitHoraire
 */
public class ConflitHoraireController {

    /**
     * Service de détection des conflits d'horaire.
     * Injecté via le constructeur pour permettre les tests unitaires.
     */
    private final ConflitHoraireService conflitService;

    /**
     * Constructeur avec injection de dépendance.
     * 
     * @param conflitService Le service de gestion des conflits
     */
    public ConflitHoraireController(ConflitHoraireService conflitService) {
        this.conflitService = conflitService;
    }

    /**
     * Récupère tous les conflits d'horaire pour un ensemble.
     * 
     * <p>Endpoint : GET /ensembles/{id}/conflits</p>
     * 
     * @param ctx Le contexte Javalin contenant la requête HTTP
     */
    public void getConflits(Context ctx) {
        try {
            String ensembleId = ctx.pathParam("id");
            
            if (ensembleId == null || ensembleId.trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "L'ID de l'ensemble est requis"));
                return;
            }

            List<ConflitHoraire> conflits = conflitService.detecterConflits(ensembleId);
            ctx.json(Map.of(
                "ensembleId", ensembleId,
                "conflits", conflits,
                "nombreConflits", conflits.size()
            ));
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }

    /**
     * Récupère un résumé statistique des conflits pour un ensemble.
     * 
     * <p>Endpoint : GET /ensembles/{id}/conflits/resume</p>
     * 
     * @param ctx Le contexte Javalin contenant la requête HTTP
     */
    public void getResumeConflits(Context ctx) {
        try {
            String ensembleId = ctx.pathParam("id");
            
            if (ensembleId == null || ensembleId.trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "L'ID de l'ensemble est requis"));
                return;
            }

            Map<String, Object> resume = conflitService.getResumeConflits(ensembleId);
            ctx.json(resume);
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }

    /**
     * Récupère les conflits groupés par jour de la semaine.
     * 
     * <p>Endpoint : GET /ensembles/{id}/conflits/par-jour</p>
     * 
     * @param ctx Le contexte Javalin contenant la requête HTTP
     */
    public void getConflitsParJour(Context ctx) {
        try {
            String ensembleId = ctx.pathParam("id");
            
            if (ensembleId == null || ensembleId.trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "L'ID de l'ensemble est requis"));
                return;
            }

            Map<String, List<ConflitHoraire>> conflitsParJour = conflitService.getConflitsParJour(ensembleId);
            ctx.json(Map.of(
                "ensembleId", ensembleId,
                "conflitsParJour", conflitsParJour
            ));
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }

    /**
     * Génère des suggestions pour résoudre les conflits.
     * 
     * <p>Endpoint : GET /ensembles/{id}/conflits/suggestions</p>
     * 
     * @param ctx Le contexte Javalin contenant la requête HTTP
     */
    public void getSuggestions(Context ctx) {
        try {
            String ensembleId = ctx.pathParam("id");
            
            if (ensembleId == null || ensembleId.trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "L'ID de l'ensemble est requis"));
                return;
            }

            Map<String, Object> suggestions = conflitService.genererSuggestionsResolution(ensembleId);
            ctx.json(suggestions);
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }

    /**
     * Vérifie si l'ajout d'un cours créerait des conflits.
     * 
     * <p>Endpoint : POST /ensembles/{id}/conflits/verifier-ajout</p>
     * <p>Body JSON requis :</p>
     * <pre>
     * {
     *   "courseId": "IFT2255",
     *   "horaires": [...]
     * }
     * </pre>
     * 
     * @param ctx Le contexte Javalin contenant la requête HTTP
     */
    public void verifierAjoutCours(Context ctx) {
        try {
            String ensembleId = ctx.pathParam("id");
            
            if (ensembleId == null || ensembleId.trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "L'ID de l'ensemble est requis"));
                return;
            }

            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            String courseId = (String) body.get("courseId");
            
            if (courseId == null || courseId.trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "L'ID du cours est requis"));
                return;
            }

            List<Horaire> horaires = new ArrayList<>();
            
            Map<String, Object> resultat = conflitService.verifierAjoutCours(ensembleId, courseId, horaires);
            ctx.json(resultat);
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }

    /**
     * Vérifie rapidement si un ensemble a des conflits.
     * 
     * <p>Endpoint : GET /ensembles/{id}/conflits/has-conflits</p>
     * 
     * @param ctx Le contexte Javalin contenant la requête HTTP
     */
    public void hasConflits(Context ctx) {
        try {
            String ensembleId = ctx.pathParam("id");
            
            if (ensembleId == null || ensembleId.trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "L'ID de l'ensemble est requis"));
                return;
            }

            boolean hasConflits = conflitService.hasConflits(ensembleId);
            int nombreConflits = conflitService.detecterConflits(ensembleId).size();
            
            ctx.json(Map.of(
                "ensembleId", ensembleId,
                "hasConflits", hasConflits,
                "nombreConflits", nombreConflits
            ));
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }

    /**
     * Invalide le cache des conflits pour un ensemble.
     * 
     * <p>Endpoint : DELETE /ensembles/{id}/conflits/cache</p>
     * 
     * @param ctx Le contexte Javalin contenant la requête HTTP
     */
    public void invaliderCache(Context ctx) {
        try {
            String ensembleId = ctx.pathParam("id");
            
            if (ensembleId == null || ensembleId.trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "L'ID de l'ensemble est requis"));
                return;
            }

            conflitService.invaliderCache(ensembleId);
            ctx.json(Map.of(
                "message", "Cache invalidé pour l'ensemble " + ensembleId,
                "ensembleId", ensembleId
            ));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }
}
