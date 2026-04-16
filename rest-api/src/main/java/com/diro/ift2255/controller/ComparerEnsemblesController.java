package com.diro.ift2255.controller;

import com.diro.ift2255.model.ComparaisonEnsembles;
import com.diro.ift2255.service.ComparerEnsemblesService;
import io.javalin.http.Context;

import java.util.*;

/**
 * Contrôleur REST pour la comparaison d'ensembles de cours (CU#12).
 * Gère les endpoints liés à la comparaison d'ensembles.
 * 
 * <p>Ce contrôleur expose les endpoints HTTP suivants :</p>
 * <ul>
 *   <li>GET /ensembles/comparer?id1={id1}&amp;id2={id2} - Comparer deux ensembles</li>
 *   <li>POST /ensembles/comparer - Comparer plusieurs ensembles</li>
 *   <li>GET /ensembles/comparer/tableau - Tableau comparatif</li>
 *   <li>POST /ensembles/comparer/personnalise - Recommandation personnalisée</li>
 *   <li>GET /ensembles/{id}/comparaisons - Comparaisons d'un ensemble</li>
 *   <li>DELETE /ensembles/comparer/cache - Invalider le cache</li>
 * </ul>
 * 
 * @author Notre équipe, l'équipe 10
 * @version 3.0
 * @since 2025-12-27
 * @see ComparerEnsemblesService
 * @see ComparaisonEnsembles
 */
public class ComparerEnsemblesController {

    /**
     * Service de comparaison d'ensembles de cours.
     * Injecté via le constructeur pour permettre les tests unitaires.
     */
    private final ComparerEnsemblesService comparerService;

    /**
     * Constructeur avec injection de dépendance.
     * 
     * @param comparerService Le service de comparaison d'ensembles
     */
    public ComparerEnsemblesController(ComparerEnsemblesService comparerService) {
        this.comparerService = comparerService;
    }

    /**
     * Compare deux ensembles de cours.
     * 
     * <p>Endpoint : GET /ensembles/comparer?id1={id1}&amp;id2={id2}</p>
     * 
     * @param ctx Le contexte Javalin contenant la requête HTTP
     */
    public void comparerDeuxEnsembles(Context ctx) {
        try {
            String id1 = ctx.queryParam("id1");
            String id2 = ctx.queryParam("id2");

            if (id1 == null || id1.trim().isEmpty() || id2 == null || id2.trim().isEmpty()) {
                ctx.status(400).json(Map.of(
                    "error", "Les paramètres id1 et id2 sont requis"
                ));
                return;
            }

            if (id1.equals(id2)) {
                ctx.status(400).json(Map.of(
                    "error", "Les deux ensembles doivent être différents"
                ));
                return;
            }

            ComparaisonEnsembles comparaison = comparerService.comparerEnsembles(id1, id2);
            ctx.json(comparaison);
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }

    /**
     * Compare plusieurs ensembles de cours.
     * 
     * <p>Endpoint : POST /ensembles/comparer</p>
     * <p>Body JSON requis :</p>
     * <pre>
     * {
     *   "ensembleIds": ["ENS-00001", "ENS-00002", "ENS-00003"]
     * }
     * </pre>
     * 
     * @param ctx Le contexte Javalin contenant la requête HTTP
     */
    public void comparerPlusieursEnsembles(Context ctx) {
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            
            @SuppressWarnings("unchecked")
            List<String> ensembleIds = (List<String>) body.get("ensembleIds");

            if (ensembleIds == null || ensembleIds.size() < 2) {
                ctx.status(400).json(Map.of(
                    "error", "Au moins 2 ensembles sont requis pour la comparaison"
                ));
                return;
            }

            Map<String, Object> resultat = comparerService.comparerPlusieursEnsembles(ensembleIds);
            ctx.json(resultat);
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }

    /**
     * Génère un tableau comparatif entre deux ensembles.
     * 
     * <p>Endpoint : GET /ensembles/comparer/tableau?id1={id1}&amp;id2={id2}</p>
     * 
     * @param ctx Le contexte Javalin contenant la requête HTTP
     */
    public void getTableauComparatif(Context ctx) {
        try {
            String id1 = ctx.queryParam("id1");
            String id2 = ctx.queryParam("id2");

            if (id1 == null || id1.trim().isEmpty() || id2 == null || id2.trim().isEmpty()) {
                ctx.status(400).json(Map.of(
                    "error", "Les paramètres id1 et id2 sont requis"
                ));
                return;
            }

            Map<String, Object> tableau = comparerService.getTableauComparatif(id1, id2);
            ctx.json(tableau);
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }

    /**
     * Génère une recommandation personnalisée basée sur les préférences.
     * 
     * <p>Endpoint : POST /ensembles/comparer/personnalise</p>
     * <p>Body JSON requis :</p>
     * <pre>
     * {
     *   "ensembleIds": ["ENS-00001", "ENS-00002"],
     *   "preferences": {
     *     "priorite": "chargeMinimale",
     *     "maxCours": 4
     *   }
     * }
     * </pre>
     * 
     * <p>Priorités supportées : chargeMinimale, sansConflits, maxCredits, equilibre</p>
     * 
     * @param ctx Le contexte Javalin contenant la requête HTTP
     */
    public void getRecommandationPersonnalisee(Context ctx) {
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            
            @SuppressWarnings("unchecked")
            List<String> ensembleIds = (List<String>) body.get("ensembleIds");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> preferences = (Map<String, Object>) body.get("preferences");

            if (ensembleIds == null || ensembleIds.isEmpty()) {
                ctx.status(400).json(Map.of(
                    "error", "Au moins un ensemble est requis"
                ));
                return;
            }

            if (preferences == null) {
                preferences = new HashMap<>();
            }

            Map<String, Object> recommandation = comparerService.genererRecommandationPersonnalisee(
                ensembleIds, preferences);
            ctx.json(recommandation);
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }

    /**
     * Récupère toutes les comparaisons impliquant un ensemble.
     * 
     * <p>Endpoint : GET /ensembles/{id}/comparaisons</p>
     * 
     * @param ctx Le contexte Javalin contenant la requête HTTP
     */
    public void getComparaisonsPourEnsemble(Context ctx) {
        try {
            String ensembleId = ctx.pathParam("id");

            if (ensembleId == null || ensembleId.trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "L'ID de l'ensemble est requis"));
                return;
            }

            List<ComparaisonEnsembles> comparaisons = comparerService.getComparaisonsPourEnsemble(ensembleId);
            ctx.json(Map.of(
                "ensembleId", ensembleId,
                "comparaisons", comparaisons,
                "nombreComparaisons", comparaisons.size()
            ));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }

    /**
     * Invalide le cache de comparaison pour un ensemble.
     * 
     * <p>Endpoint : DELETE /ensembles/comparer/cache?id={ensembleId}</p>
     * 
     * @param ctx Le contexte Javalin contenant la requête HTTP
     */
    public void invaliderCache(Context ctx) {
        try {
            String ensembleId = ctx.queryParam("id");

            if (ensembleId == null || ensembleId.trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "L'ID de l'ensemble est requis"));
                return;
            }

            comparerService.invaliderCache(ensembleId);
            ctx.json(Map.of(
                "message", "Cache de comparaison invalidé pour l'ensemble " + ensembleId,
                "ensembleId", ensembleId
            ));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Erreur interne: " + e.getMessage()));
        }
    }
}