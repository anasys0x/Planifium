package com.diro.ift2255.service;

import com.diro.ift2255.model.Course;
import com.diro.ift2255.model.Program;
import com.diro.ift2255.model.dto.CheckEligibilityResponse;
import com.diro.ift2255.model.dto.CourseScheduleResponse;
import com.diro.ift2255.model.dto.SectionSchedule;
import com.diro.ift2255.model.dto.ActivitySchedule;
import com.diro.ift2255.model.dto.ScheduleEntry;
import com.diro.ift2255.util.HttpClientApi;
import com.diro.ift2255.util.HttpClientApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.diro.ift2255.util.StringUtil;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CourseService {

    private final HttpClientApi clientApi;
    private final ObjectMapper objectMapper;

    private static final String BASE_URL = "https://planifium-api.onrender.com/api/v1/courses";
    private static final String PROGRAMS_URL = "https://planifium-api.onrender.com/api/v1/programs";

    public CourseService(HttpClientApi clientApi) {
        this.clientApi = clientApi;
        this.objectMapper = new ObjectMapper();
    }

    /** Fetch all courses */
    public List<Course> getAllCourses(Map<String, String> queryParams) {
        Map<String, String> params = (queryParams == null) ? Collections.emptyMap() : queryParams;

        URI uri = HttpClientApi.buildUri(BASE_URL, params);
        List<Course> courses = clientApi.get(uri, new TypeReference<List<Course>>() {});
        return courses;
    }

    /** Fetch a course by ID */
    public Optional<Course> getCourseById(String courseId) {
        return getCourseById(courseId, null);
    }

    /** Fetch a course by ID with optional query params */
    public Optional<Course> getCourseById(String courseId, Map<String, String> queryParams) {
        Map<String, String> params = (queryParams == null) ? Collections.emptyMap() : queryParams;
        URI uri = HttpClientApi.buildUri(BASE_URL + "/" + courseId, params);

        try {
            Course course = clientApi.get(uri, Course.class);
            return Optional.of(course);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /** Advanced course search (local filtering over Planifium list) */
public List<Course> searchCoursesAdvanced(String keyword) {
    if (keyword == null || keyword.isBlank()) {
        return List.of();
    }

    String kw = StringUtil.normalize(keyword);

    URI uri = HttpClientApi.buildUri(BASE_URL, Map.of());
    List<Course> allCourses = clientApi.get(uri, new TypeReference<List<Course>>() {});
    if (allCourses == null) return List.of();

    return allCourses.stream()
            .filter(c ->
                    StringUtil.normalize(c.getId()).contains(kw)
                 || StringUtil.normalize(c.getName()).contains(kw)
                 || StringUtil.normalize(c.getDescription()).contains(kw)
            )
            .toList();
}

    /** Compare courses by IDs */
    public Map<String, Object> compareCourses(List<String> ids) {
        Map<String, Object> result = new HashMap<>();
        List<Course> courses = new ArrayList<>();

        if (ids != null) {
            for (String id : ids) {
                getCourseById(id).ifPresent(courses::add);
            }
        }

        result.put("courses", courses);
        return result;
    }

    /** Search programs by keyword (returns only id + name) */
    public List<Map<String, String>> searchPrograms(String keyword) {
        Map<String, String> params = new HashMap<>();
        params.put("name", keyword);

        URI uri = HttpClientApi.buildUri(PROGRAMS_URL, params);

        List<Map<String, Object>> raw =
                clientApi.get(uri, new TypeReference<List<Map<String, Object>>>() {});

        if (raw == null) return List.of();

        List<Map<String, String>> result = new ArrayList<>();
        for (Map<String, Object> p : raw) {
            Object id = p.get("id");
            Object name = p.get("name");
            if (id != null && name != null) {
                result.add(Map.of(
                        "id", id.toString(),
                        "name", name.toString()
                ));
            }
        }
        return result;
    }

    /** Search programs by name (returns full raw objects) */
    public List<Map<String, Object>> searchProgramsByName(String name) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);

        URI uri = HttpClientApi.buildUri(PROGRAMS_URL, params);

        return clientApi.get(
                uri,
                new TypeReference<List<Map<String, Object>>>() {}
        );
    }

    /**
     * Récupère tous les cours pour un programme spécifique.
     * 
     * @param programCode Le code du programme (ex: "117510")
     * @return La liste des cours associés au programme spécifié
     */
    public List<Course> getCoursesInProgram(String programCode) {
        // Si c'est un code de programme numérique (6 chiffres)
        if (programCode.matches("^\\d{6}$")) {
            try {
                Map<String, String> params = new HashMap<>();
                params.put("programs_list", programCode);
                params.put("include_courses_detail", "true");
                params.put("response_level", "full");
                
                URI uri = HttpClientApi.buildUri(PROGRAMS_URL, params);
                
                // L'API retourne un seul objet Program
                Program program = clientApi.get(uri, Program.class);
                if (program != null) {
                    return extractCoursesFromProgram(program);
                }
            } catch (RuntimeException e) {
                System.err.println("Erreur lors de la récupération du programme " + programCode + ": " + e.getMessage());
            }
        }
        
        // Si ce n'est pas un code de programme valide ou si rien ne fonctionne, retourner liste vide
        return Collections.emptyList();
    }
    
    /**
     * Extrait les cours d'un objet Program.
     * Les cours peuvent être des objets Course, des Maps, ou des IDs (strings).
     */
    private List<Course> extractCoursesFromProgram(Program program) {
        if (program == null || program.getCourses() == null || program.getCourses().isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Course> courses = new ArrayList<>();
        for (Object courseObj : program.getCourses()) {
            Course course = extractCourseFromObject(courseObj);
            if (course != null) {
                courses.add(course);
            }
        }
        return courses;
    }
    
    /**
     * Extrait un Course depuis un Object (peut être Course, Map, ou String ID)
     */
    private Course extractCourseFromObject(Object courseObj) {
        if (courseObj instanceof Course course) {
            return course;
        } else if (courseObj instanceof Map<?, ?> map) {
            try {
                return objectMapper.convertValue(map, Course.class);
            } catch (IllegalArgumentException e) {
                return null;
            }
        } else if (courseObj instanceof String courseId) {
            // Si c'est juste un ID, récupérer le cours complet
            return getCourseById(courseId).orElse(null);
        }
        return null;
    }

    /**
     * Récupère les cours pour un trimestre spécifique, limités à un programme si fourni.
     * Effectue un filtrage côté client sur la propriété available_terms.
     * 
     * @param trimester Le code du trimestre (ex: "H25", "A24", "E24")
     * @param programCode Le code du programme (ex: "117510") - optionnel
     * @return La liste des cours associés au trimestre (et au programme si fourni)
     */
        public List<Course> getCoursesByTrimester(String trimester, String programCode) {
        String term = trimesterCodeToTerm(trimester.toLowerCase());
        if (term == null) {
            return Collections.emptyList();
        }
        
        List<Course> courses;
        
        // 1. Récupérer les cours (soit du programme, soit tous les cours)
        if (programCode != null && programCode.matches("^\\d{6}$")) {
            courses = getCoursesInProgram(programCode);
        } else {
            // Si aucun programme spécifié, on cherche dans tous les cours
            // On demande 'response_level=full' pour être sûr d'avoir 'available_terms'
            try {
                URI uri = HttpClientApi.buildUri(BASE_URL, Map.of("response_level", "full"));
                courses = clientApi.get(uri, new TypeReference<List<Course>>() {});
            } catch (RuntimeException e) {
                System.err.println("Erreur lors de la récupération de tous les cours: " + e.getMessage());
                return Collections.emptyList();
            }
        }
        
        if (courses == null || courses.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Filtrer côté client selon le trimestre
        List<Course> filteredCourses = new ArrayList<>();
        for (Course course : courses) {
            if (course != null && isAvailableForTerm(course, term)) {
                filteredCourses.add(course);
            }
        }
        
        return filteredCourses;
    }
    
    /**
     * Convertit un code de trimestre (h25, a24, e24) en terme (winter, autumn, summer).
     */
    private String trimesterCodeToTerm(String trimesterCode) {
        if (trimesterCode == null || trimesterCode.length() < 1) {
            return null;
        }
        char firstChar = trimesterCode.toLowerCase().charAt(0);
        return switch (firstChar) {
            case 'h' -> "winter";
            case 'a' -> "autumn";
            case 'e' -> "summer";
            default -> null;
        };
    }
    
    /**
     * Vérifie si un cours est disponible pour un trimestre donné en utilisant available_terms.
     */
    private boolean isAvailableForTerm(Course course, String term) {
        if (course.getAvailable_terms() == null) {
            return false;
        }
        Boolean isAvailable = course.getAvailable_terms().get(term);
        return isAvailable != null && isAvailable;
    }

    /** Eligibility logic */
    public CheckEligibilityResponse checkEligibility(String courseId, List<String> completedCourses, int cycle) {

        Map<String, String> params = new HashMap<>();
        params.put("response_level", "full");

        Optional<Course> opt = getCourseById(courseId, params);
        if (opt.isEmpty()) {
            return new CheckEligibilityResponse(courseId.toUpperCase(), false, false,
                    List.of(), "Cours introuvable: " + courseId);
        }

        Course course = opt.get();
        List<String> prereqs = course.getPrerequisite_courses();
        if (prereqs == null) prereqs = List.of();

        Set<String> done = new HashSet<>();
        if (completedCourses != null) {
            for (String c : completedCourses) {
                if (c != null && !c.isBlank()) done.add(c.trim().toUpperCase());
            }
        }

        List<String> missing = new ArrayList<>();
        for (String p : prereqs) {
            if (p == null || p.isBlank()) continue;
            String pr = p.trim().toUpperCase();
            if (!done.contains(pr)) missing.add(pr);
        }

        boolean prereqOk = missing.isEmpty();
        boolean cycleOk = isCycleAppropriate(courseId, cycle);
        boolean eligible = prereqOk && cycleOk;

        String msg;
        if (eligible) msg = "Éligible : prérequis satisfaits et cycle approprié.";
        else if (!cycleOk && !prereqOk) msg = "Non éligible : cycle non approprié et prérequis manquants: " + missing;
        else if (!cycleOk) msg = "Non éligible : cycle non approprié.";
        else msg = "Non éligible : prérequis manquants: " + missing;

        return new CheckEligibilityResponse(courseId.toUpperCase(), eligible, cycleOk, missing, msg);
    }

    private boolean isCycleAppropriate(String courseId, int cycle) {
        Matcher m = Pattern.compile("(\\d{4})").matcher(courseId);
        if (!m.find()) return true;

        int num = Integer.parseInt(m.group(1));
        boolean isGrad = num >= 5000;

        return (cycle == 1) ? !isGrad : isGrad;
    }

    /**
     * Récupère l'horaire d'un cours pour un trimestre donné.
     * Distingue les sections et les types d'activité.
     * 
     * @param courseId L'ID du cours (ex: "IFT2255")
     * @param semester Le code du trimestre (ex: "A25", "H26", "E24")
     * @return La réponse structurée avec les sections et activités, ou null si erreur
     */
    public CourseScheduleResponse getCourseSchedule(String courseId, String semester) {
        if (courseId == null || courseId.isBlank() || semester == null || semester.isBlank()) {
            return null;
        }

        try {
            Map<String, String> params = new HashMap<>();
            params.put("include_schedule", "true");
            params.put("schedule_semester", semester.toUpperCase());
            
            URI uri = HttpClientApi.buildUri(BASE_URL + "/" + courseId.toUpperCase(), params);
            HttpClientApiResponse rawResponse = clientApi.get(uri);
            
            if (rawResponse.getStatusCode() < 200 || rawResponse.getStatusCode() >= 300) {
                return null;
            }

            String body = rawResponse.getBody();
            if (body == null || body.isBlank()) {
                return null;
            }

            CourseScheduleResponse response = new CourseScheduleResponse(courseId.toUpperCase(), semester.toUpperCase());
            parseScheduleFromJSON(body, response);
            return response;
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de l'horaire pour " + courseId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parse le schedule depuis une réponse JSON.
     * Structure réelle: schedules[0].sections[].volets[].activities[]
     */
    private void parseScheduleFromJSON(String jsonBody, CourseScheduleResponse response) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonBody);
            
            if (!rootNode.has("schedules")) {
                return;
            }

            JsonNode schedulesArray = rootNode.get("schedules");
            if (!schedulesArray.isArray() || schedulesArray.size() == 0) {
                return;
            }

            JsonNode scheduleObj = schedulesArray.get(0);
            if (scheduleObj == null || !scheduleObj.has("sections")) {
                return;
            }

            JsonNode sectionsArray = scheduleObj.get("sections");
            if (!sectionsArray.isArray()) {
                return;
            }

            for (JsonNode sectionNode : sectionsArray) {
                String sectionCode = sectionNode.has("name") ? sectionNode.get("name").asText() : null;
                if (sectionCode == null || sectionCode.isBlank()) continue;

                SectionSchedule sectionSchedule = new SectionSchedule(sectionCode);

                if (sectionNode.has("volets") && sectionNode.get("volets").isArray()) {
                    for (JsonNode voletNode : sectionNode.get("volets")) {
                        String activityType = voletNode.has("name") ? voletNode.get("name").asText() : null;
                        if (activityType == null || activityType.isBlank()) continue;

                        ActivitySchedule activitySchedule = new ActivitySchedule(activityType);

                        if (voletNode.has("activities") && voletNode.get("activities").isArray()) {
                            for (JsonNode activityNode : voletNode.get("activities")) {
                                if (activityNode.has("days") && activityNode.has("start_time") && activityNode.has("end_time")) {
                                    String startTime = activityNode.get("start_time").asText();
                                    String endTime = activityNode.get("end_time").asText();
                                    String room = activityNode.has("room") ? activityNode.get("room").asText() : null;
                                    
                                    for (JsonNode dayNode : activityNode.get("days")) {
                                        String day = convertDayAbbreviation(dayNode.asText());
                                        activitySchedule.addEntry(new ScheduleEntry(day, startTime, endTime, room));
                                    }
                                }
                            }
                        }

                        if (!activitySchedule.entries.isEmpty()) {
                            sectionSchedule.addActivity(activitySchedule);
                        }
                    }
                }

                if (!sectionSchedule.activities.isEmpty()) {
                    response.addSection(sectionSchedule);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du parsing JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Convertit les abréviations de jours françaises en noms complets.
     */
    private String convertDayAbbreviation(String abbrev) {
        if (abbrev == null) return "N/A";
        switch (abbrev.toUpperCase()) {
            case "LU": return "Lundi";
            case "MA": return "Mardi";
            case "ME": return "Mercredi";
            case "JE": return "Jeudi";
            case "VE": return "Vendredi";
            case "SA": return "Samedi";
            case "DI": return "Dimanche";
            default: return abbrev;
        }
    }


}
