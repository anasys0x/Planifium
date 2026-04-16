package com.diro.ift2255.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.diro.ift2255.model.Course;
import com.diro.ift2255.model.Review;
import com.diro.ift2255.model.dto.CheckEligibilityRequest;
import com.diro.ift2255.model.dto.CourseResultDto;
import com.diro.ift2255.model.dto.CourseScheduleResponse;
import com.diro.ift2255.service.CourseService;
import com.diro.ift2255.service.ResultsService;
import com.diro.ift2255.service.ReviewService;
import com.diro.ift2255.util.ResponseUtil;
import com.diro.ift2255.util.ValidationUtil;

import io.javalin.http.Context;

/**
 * Contrôleur REST pour les cours (catalogue, recherche, horaire, admissibilité, résultats et comparaison).
 * Gère les endpoints liés aux cours et applique une validation simple des entrées.
 *
 * <p>Ce contrôleur expose notamment :</p>
 * <ul>
 *   <li>GET /programs/search?q=... - Recherche de programmes</li>
 *   <li>GET /courses/in-program?program=... - Cours d'un programme</li>
 *   <li>GET /courses/search-advanced?q=... - Recherche avancée de cours</li>
 *   <li>GET /programs/by-name?name=... - Recherche de programmes par nom (optionnel)</li>
 *   <li>GET /courses/semester/{code}?program=... - Cours d'un trimestre</li>
 *   <li>GET /courses - Liste des cours (avec filtres optionnels)</li>
 *   <li>GET /courses/{id} - Détails d'un cours</li>
 *   <li>GET /courses/{id}/schedule?semester=... - Horaire d'un cours</li>
 *   <li>POST /courses/{id}/eligibility - Vérification d'admissibilité</li>
 *   <li>GET /courses/{id}/results - Résultats académiques agrégés</li>
 *   <li>GET /courses/compare?id=...&amp;id=... - Comparaison de deux cours</li>
 * </ul>
 *
 * <p>Notes d'implémentation :</p>
 * <ul>
 *   <li>{@code resultsService} et {@code reviewService} sont optionnels et peuvent être {@code null}
 *       (par exemple selon les tests, ou selon les fonctionnalités branchées).</li>
 *   <li>La logique métier est déléguée aux services; le contrôleur gère surtout l'entrée/sortie HTTP.</li>
 * </ul>
 *
 * @author Notre équipe
 * @version 1.0
 * @since 2025-12-28
 * @see CourseService
 * @see ResultsService
 * @see ReviewService
 */
public class CourseController {

    /** Service principal (catalogue, recherche, horaire, admissibilité, comparaison "catalogue"). */
    private final CourseService service;

    /** Service optionnel pour les résultats académiques (peut être null). */
    private final ResultsService resultsService;

    /** Service optionnel pour les avis étudiants (peut être null). */
    private final ReviewService reviewService;

    /**
     * Constructeur conservé pour les tests existants.
     *
     * @param service service principal des cours
     */
    public CourseController(CourseService service) {
        this(service, null, null);
    }

    /**
     * Constructeur intermédiaire (si tu branches uniquement les résultats).
     *
     * @param service service principal des cours
     * @param resultsService service des résultats académiques (optionnel)
     */
    public CourseController(CourseService service, ResultsService resultsService) {
        this(service, resultsService, null);
    }

    /**
     * Constructeur complet (souvent utilisé dans Routes).
     *
     * @param service service principal des cours
     * @param resultsService service des résultats académiques (optionnel)
     * @param reviewService service des avis étudiants (optionnel)
     */
    public CourseController(CourseService service, ResultsService resultsService, ReviewService reviewService) {
        this.service = service;
        this.resultsService = resultsService;
        this.reviewService = reviewService;
    }

    // ---------------- PROGRAMS / SEARCH ----------------

    /**
     * Recherche des programmes à partir d'une chaîne libre.
     *
     * <p>Endpoint : GET /programs/search?q=...</p>
     *
     * @param ctx le contexte Javalin contenant la query param {@code q}
     */
    public void searchPrograms(Context ctx) {
        String q = ctx.queryParam("q");

        if (q == null || q.isBlank()) {
            ctx.status(422).json(ResponseUtil.formatError("Paramètre 'q' requis."));
            return;
        }

        ctx.json(service.searchPrograms(q));
    }

    /**
     * Récupère tous les cours associés à un programme.
     *
     * <p>Endpoint : GET /courses/in-program?program=...</p>
     *
     * @param ctx le contexte Javalin (query param {@code program})
     */
    public void getCoursesInProgram(Context ctx) {
        String program = ctx.queryParam("program");

        if (program == null || program.isBlank()) {
            ctx.status(422).json(ResponseUtil.formatError("Paramètre 'program' requis."));
            return;
        }

        try {
            List<Course> courses = service.getCoursesInProgram(program);
            if (courses == null || courses.isEmpty()) {
                ctx.status(404).json(ResponseUtil.formatError(
                        "Aucun cours trouvé pour le programme: " + program
                ));
                return;
            }
            ctx.json(courses);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(ResponseUtil.formatError(
                    "Erreur lors de la récupération des cours: " + e.getMessage()
            ));
        }
    }

    /**
     * Recherche avancée de cours (sigle, titre, mots-clés, etc.).
     *
     * <p>Endpoint : GET /courses/search-advanced?q=...</p>
     *
     * @param ctx le contexte Javalin contenant la query param {@code q}
     */
    public void searchCoursesAdvanced(Context ctx) {
        String q = ctx.queryParam("q");

        if (q == null || q.isBlank()) {
            ctx.status(422).json(ResponseUtil.formatError("Paramètre 'q' requis."));
            return;
        }

        ctx.json(service.searchCoursesAdvanced(q));
    }

    /**
     * Recherche des programmes par nom (optionnel selon tes routes).
     *
     * <p>Endpoint : GET /programs/by-name?name=...</p>
     *
     * @param ctx le contexte Javalin contenant la query param {@code name}
     */
    public void searchProgramsByName(Context ctx) {
        String name = ctx.queryParam("name");

        if (name == null || name.trim().isEmpty()) {
            ctx.status(400).json(Map.of("error", "Paramètre 'name' manquant"));
            return;
        }

        ctx.json(service.searchProgramsByName(name.trim()));
    }

    /**
     * Récupère les cours offerts pour un trimestre donné, avec option de filtrer par programme.
     *
     * <p>Endpoint : GET /courses/semester/{code}?program=...</p>
     *
     * <p>Format attendu pour {@code code} : H25, A24 ou E24.</p>
     *
     * @param ctx le contexte Javalin (path param {@code code}, query param optionnel {@code program})
     */
    public void getCoursesBySemester(Context ctx) {
        String semester = ctx.pathParam("code");
        String program = ctx.queryParam("program");

        if (!ValidationUtil.isValidSemester(semester)) {
            ctx.status(400).json(ResponseUtil.formatError(
                    "Format de semestre invalide. Utilisez le format H25, A24 ou E24 (ex: H25 pour Hiver 2025)."
            ));
            return;
        }

        try {
            List<Course> courses = service.getCoursesByTrimester(semester, program);
            if (courses == null || courses.isEmpty()) {
                String message = program != null
                        ? "Aucun cours trouvé pour le semestre " + semester + " dans le programme " + program
                        : "Aucun cours trouvé pour le semestre: " + semester + ". Spécifiez un programme avec ?program=117510";
                ctx.status(404).json(ResponseUtil.formatError(message));
                return;
            }
            ctx.json(courses);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(ResponseUtil.formatError(
                    "Erreur lors de la récupération des cours: " + e.getMessage()
            ));
        }
    }

    // ---------------- COURSES ----------------

    /**
     * Retourne la liste des cours, avec support de filtres optionnels via query params.
     *
     * <p>Endpoint : GET /courses</p>
     *
     * @param ctx le contexte Javalin (query params transmis au service)
     */
    public void getAllCourses(Context ctx) {
        Map<String, String> queryParams = extractQueryParams(ctx);
        List<Course> courses = service.getAllCourses(queryParams);
        ctx.json(courses);
    }

    /**
     * Retourne le détail d'un cours à partir de son identifiant.
     *
     * <p>Endpoint : GET /courses/{id}</p>
     *
     * @param ctx le contexte Javalin (path param {@code id})
     */
    public void getCourseById(Context ctx) {
        String id = ctx.pathParam("id");

        if (!validateCourseId(id)) {
            ctx.status(400).json(ResponseUtil.formatError("Le paramètre id n'est pas valide."));
            return;
        }

        Optional<Course> course = service.getCourseById(id);
        if (course.isPresent()) {
            ctx.json(course.get());
        } else {
            ctx.status(404).json(ResponseUtil.formatError("Aucun cours ne correspond à l'ID: " + id));
        }
    }

    /**
     * Récupère l'horaire d'un cours pour un trimestre donné (sections + types d'activité).
     *
     * <p>Endpoint : GET /courses/{id}/schedule?semester=...</p>
     *
     * @param ctx le contexte Javalin (path param {@code id}, query param {@code semester})
     */
    public void getCourseSchedule(Context ctx) {
        String courseId = ctx.pathParam("id");
        String semester = ctx.queryParam("semester");

        if (!validateCourseId(courseId)) {
            ctx.status(400).json(ResponseUtil.formatError("Le paramètre id n'est pas valide."));
            return;
        }

        if (semester == null || semester.isBlank()) {
            ctx.status(400).json(ResponseUtil.formatError("Le paramètre 'semester' est requis (ex: A25, H26, E24)."));
            return;
        }

        if (!ValidationUtil.isValidSemester(semester)) {
            ctx.status(400).json(ResponseUtil.formatError(
                    "Format de trimestre invalide. Utilisez le format H25, A24 ou E24 (ex: H25 pour Hiver 2025)."
            ));
            return;
        }

        try {
            CourseScheduleResponse schedule = service.getCourseSchedule(courseId, semester);

            if (schedule == null) {
                ctx.status(404).json(ResponseUtil.formatError(
                        "Aucun horaire trouvé pour le cours " + courseId + " au trimestre " + semester + "."
                ));
                return;
            }

            // On renvoie quand même l'objet, même si sections est vide.
            ctx.status(200).json(schedule);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(ResponseUtil.formatError(
                    "Erreur lors de la récupération de l'horaire: " + e.getMessage()
            ));
        }
    }

    // ---------------- ELIGIBILITY ----------------

    /**
     * Vérifie l'admissibilité à un cours en fonction des cours complétés et du cycle.
     *
     * <p>Endpoint : POST /courses/{id}/eligibility</p>
     *
     * <p>Body JSON attendu :</p>
     * <pre>
     * {
     *   "completed_courses": ["IFT1005", "IFT2015"],
     *   "cycle": 1
     * }
     * </pre>
     *
     * @param ctx le contexte Javalin (path param {@code id} + body JSON)
     */
    public void checkEligibility(Context ctx) {
        CheckEligibilityRequest req;
        try {
            req = ctx.bodyAsClass(CheckEligibilityRequest.class);
        } catch (Exception e) {
            ctx.status(422).json(ResponseUtil.formatError("Body JSON invalide."));
            return;
        }

        if (req == null || req.completed_courses == null) {
            ctx.status(422).json(ResponseUtil.formatError("completed_courses est requis (tableau)."));
            return;
        }

        int cycle = (req.cycle == null) ? 1 : req.cycle;
        if (cycle < 1 || cycle > 3) {
            ctx.status(422).json(ResponseUtil.formatError("cycle invalide. Valeurs permises: 1, 2, 3."));
            return;
        }

        String courseId = ctx.pathParam("id");
        var result = service.checkEligibility(courseId, req.completed_courses, cycle);

        if (result.message != null && result.message.startsWith("Cours introuvable")) {
            ctx.status(404).json(ResponseUtil.formatError("Aucun cours ne correspond à l'ID: " + courseId));
            return;
        }

        ctx.status(200).json(result);
    }

    // ---------------- RESULTS ----------------

    /**
     * Retourne les résultats académiques agrégés d'un cours (si le service est configuré).
     *
     * <p>Endpoint : GET /courses/{id}/results</p>
     *
     * @param ctx le contexte Javalin (path param {@code id})
     */
    public void getAcademicResults(Context ctx) {
        if (resultsService == null) {
            ctx.status(500).json(ResponseUtil.formatError("RésultatsService non configuré."));
            return;
        }

        String courseId = ctx.pathParam("id");
        if (!validateCourseId(courseId)) {
            ctx.status(400).json(ResponseUtil.formatError("id de cours invalide."));
            return;
        }

        try {
            CourseResultDto dto = resultsService.getResultsForCourse(courseId);
            ctx.status(200).json(dto);
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(ResponseUtil.formatError(e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(ResponseUtil.formatError(
                    "Erreur interne lors de la lecture des résultats académiques."
            ));
        }
    }

    // ---------------- COMPARE (catalog + reviews + results) ----------------

    /**
     * Compare deux cours en combinant :
     * <ul>
     *   <li>les informations catalogue (via {@link CourseService#compareCourses(List)})</li>
     *   <li>des statistiques d'avis (si {@code reviewService} est configuré)</li>
     *   <li>des statistiques de résultats académiques (si {@code resultsService} est configuré)</li>
     * </ul>
     *
     * <p>Endpoint : GET /courses/compare?id=...&amp;id=...</p>
     *
     * @param ctx le contexte Javalin contenant deux query params {@code id}
     */
    public void compareCourses(Context ctx) {
        List<String> ids = ctx.queryParams("id");

        if (ids == null || ids.size() < 2) {
            ctx.status(422).json(ResponseUtil.formatError(
                    "Il faut fournir 2 paramètres id: /courses/compare?id=IFT2255&id=IFT3150"
            ));
            return;
        }
        if (ids.size() > 2) {
            ids = ids.subList(0, 2);
        }

        Map<String, Object> base = service.compareCourses(ids);

        Object coursesObj = base.get("courses");
        if (!(coursesObj instanceof List<?> coursesList) || coursesList.size() < 2) {
            ctx.status(404).json(ResponseUtil.formatError("Un ou plusieurs cours introuvables."));
            return;
        }

        List<Course> courses = new ArrayList<>();
        for (Object o : coursesList) {
            if (o instanceof Course c) courses.add(c);
        }
        if (courses.size() < 2) {
            ctx.status(404).json(ResponseUtil.formatError("Un ou plusieurs cours introuvables."));
            return;
        }

        Course c1 = courses.get(0);
        Course c2 = courses.get(1);

        Map<String, Object> reviewStats = new HashMap<>();
        if (reviewService != null) {
            reviewStats.put(c1.getId().toUpperCase(), computeReviewStats(reviewService.getReviews(c1.getId())));
            reviewStats.put(c2.getId().toUpperCase(), computeReviewStats(reviewService.getReviews(c2.getId())));
        } else {
            reviewStats.put("warning", "ReviewService non configuré.");
        }

        Map<String, Object> resultsStats = new HashMap<>();
        if (resultsService != null) {
            resultsStats.put(c1.getId().toUpperCase(), computeResultsStatsSafe(c1.getId()));
            resultsStats.put(c2.getId().toUpperCase(), computeResultsStatsSafe(c2.getId()));
        } else {
            resultsStats.put("warning", "ResultsService non configuré.");
        }

        Map<String, Object> comparison = buildComparisonSummary(c1, c2, reviewStats, resultsStats);

        base.put("reviewStats", reviewStats);
        base.put("resultsStats", resultsStats);
        base.put("comparison", comparison);

        ctx.json(base);
    }

    /**
     * Calcule des statistiques simples à partir d'une liste d'avis.
     *
     * @param reviews la liste d'avis (peut être null)
     * @return une map contenant {@code count}, {@code avgDifficulty} et {@code workloadEstimate}
     */
    private Map<String, Object> computeReviewStats(List<Review> reviews) {
        Map<String, Object> out = new HashMap<>();
        int n = (reviews == null) ? 0 : reviews.size();
        out.put("count", n);

        if (n == 0) {
            out.put("avgDifficulty", null);
            out.put("workloadEstimate", null);
            return out;
        }

        double sum = 0.0;
        for (Review r : reviews) sum += r.getDifficulty();

        double avg = sum / n;
        out.put("avgDifficulty", round2(avg));
        out.put("workloadEstimate", round2(avg));
        return out;
    }

    /**
     * Construit des statistiques sur les résultats académiques, sans laisser remonter d'exception.
     *
     * @param courseId identifiant du cours
     * @return une map de statistiques, ou une clé {@code error} si non disponible
     */
    private Map<String, Object> computeResultsStatsSafe(String courseId) {
        Map<String, Object> out = new HashMap<>();
        try {
            CourseResultDto dto = resultsService.getResultsForCourse(courseId);

            out.put("sigle", dto.sigle);
            out.put("moyenne", dto.moyenne);
            out.put("score", dto.score);
            out.put("difficultyIndex", round2(6.0 - dto.score));
            out.put("participants", dto.participants);
            out.put("trimestres", dto.trimestres);

        } catch (IllegalArgumentException e) {
            out.put("error", e.getMessage());
        } catch (Exception e) {
            out.put("error", "Erreur interne lors de la lecture des résultats académiques.");
        }
        return out;
    }

    /**
     * Produit une synthèse lisible des comparaisons (workload, difficulté et infos catalogue).
     *
     * @param c1 premier cours
     * @param c2 deuxième cours
     * @param reviewStats stats issues des avis
     * @param resultsStats stats issues des résultats académiques
     * @return map qui sera retournée dans le champ {@code comparison}
     */
    private Map<String, Object> buildComparisonSummary(
            Course c1, Course c2,
            Map<String, Object> reviewStats,
            Map<String, Object> resultsStats
    ) {
        Map<String, Object> out = new HashMap<>();

        String id1 = c1.getId().toUpperCase();
        String id2 = c2.getId().toUpperCase();

        Double w1 = extractDouble(reviewStats.get(id1), "workloadEstimate");
        Double w2 = extractDouble(reviewStats.get(id2), "workloadEstimate");

        Double d1 = extractDouble(resultsStats.get(id1), "difficultyIndex");
        Double d2 = extractDouble(resultsStats.get(id2), "difficultyIndex");

        out.put("higherWorkload",
                (w1 != null && w2 != null)
                        ? ((w1 > w2) ? id1 : (w2 > w1) ? id2 : "equal")
                        : "unknown");

        out.put("harderCourse",
                (d1 != null && d2 != null)
                        ? ((d1 > d2) ? id1 : (d2 > d1) ? id2 : "equal")
                        : "unknown");

        Map<String, Object> catalog = new HashMap<>();

        Map<String, Object> course1Info = new HashMap<>();
        course1Info.put("name", c1.getName() != null ? c1.getName() : "");
        course1Info.put("credits", c1.getCredits() != null ? c1.getCredits() : "");
        course1Info.put("prereqs",
                c1.getPrerequisite_courses() != null ? c1.getPrerequisite_courses() : List.of());

        Map<String, Object> course2Info = new HashMap<>();
        course2Info.put("name", c2.getName() != null ? c2.getName() : "");
        course2Info.put("credits", c2.getCredits() != null ? c2.getCredits() : "");
        course2Info.put("prereqs",
                c2.getPrerequisite_courses() != null ? c2.getPrerequisite_courses() : List.of());

        catalog.put(id1, course1Info);
        catalog.put(id2, course2Info);
        out.put("catalog", catalog);

        return out;
    }

    /**
     * Extrait une valeur numérique d'une map (si possible).
     *
     * @param maybeMap objet supposé être une map
     * @param key clé à lire
     * @return valeur sous forme de {@link Double}, ou {@code null} si indisponible
     */
    private Double extractDouble(Object maybeMap, String key) {
        if (!(maybeMap instanceof Map<?, ?> m)) return null;
        Object v = m.get(key);
        if (v instanceof Number n) return n.doubleValue();
        return null;
    }

    // ---------------- HELPERS ----------------

    /**
     * Valide grossièrement un identifiant de cours (ex: IFT2255).
     *
     * @param courseId identifiant du cours
     * @return true si l'identifiant semble valide, sinon false
     */
    private boolean validateCourseId(String courseId) {
        return courseId != null && courseId.trim().length() >= 6;
    }

    /**
     * Convertit les query params en map simple (clé -&gt; première valeur).
     *
     * @param ctx contexte Javalin
     * @return map des query params (première valeur uniquement)
     */
    private Map<String, String> extractQueryParams(Context ctx) {
        Map<String, String> queryParams = new HashMap<>();
        ctx.queryParamMap().forEach((key, values) -> {
            if (!values.isEmpty()) queryParams.put(key, values.get(0));
        });
        return queryParams;
    }

    /**
     * Arrondit à 2 décimales (utile pour éviter des réponses trop verbeuses côté API).
     *
     * @param x valeur à arrondir
     * @return valeur arrondie à 2 décimales
     */
    private double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }
}
