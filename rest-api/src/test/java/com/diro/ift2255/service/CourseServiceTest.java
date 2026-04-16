package com.diro.ift2255.service;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.diro.ift2255.model.Course;
import com.diro.ift2255.model.Program;
import com.diro.ift2255.model.dto.CourseScheduleResponse;
import com.diro.ift2255.util.HttpClientApi;
import com.diro.ift2255.util.HttpClientApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {
    
    @Mock
    private HttpClientApi mockClientApi;
    
    private CourseService courseService;
    private long testStartTime;

    @BeforeAll
    static void printHeader() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CourseService Tests");
        System.out.println("=".repeat(80));
    }

    @BeforeEach
    void setup(TestInfo testInfo) {
        courseService = new CourseService(mockClientApi);
        testStartTime = System.currentTimeMillis();

        System.out.println("\nTEST: " + testInfo.getDisplayName());
        System.out.println("    ├─ Method: " + testInfo.getTestMethod().get().getName());
        System.out.println("    ├─ Assertions:");
    }

    @AfterEach
    void tearDown(TestInfo testInfo) {
        long duration = System.currentTimeMillis() - testStartTime;
        System.out.println("    └─ Duration: " + duration + " ms");
    }

    @Test
    @DisplayName("Devrait retourner tous les cours lorsqu'aucun paramètre de requête n'est fourni")
    void testGetAllCourses() {
        // ARRANGE
        List<Course> mockCourses = Arrays.asList(
                new Course("IFT1015", "Programmation I"),
                new Course("IFT2255", "Génie logiciel"));
        
        when(mockClientApi.get(any(URI.class), any(TypeReference.class)))
                .thenReturn(mockCourses);
        
        // ACT
        List<Course> result = courseService.getAllCourses(null);
        
        // ASSERT
        try {
            assertNotNull(result, "Resultat ne devrait pas être nul");
            OK("Result is not null", false);
            
            assertEquals(2, result.size(), "Devrait y avoir 2 cours retournés");
            OK("Returned 2 courses as expected");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("Devrait retourner un cours par son ID")
    void testGetCourseByIdExists() {
        // ARRANGE
        String courseId = "IFT2255";
        Course mockCourse = new Course(courseId, "Génie logiciel");
        
        when(mockClientApi.get(any(URI.class), eq(Course.class)))
                .thenReturn(mockCourse);
        
        // ACT
        Optional<Course> result = courseService.getCourseById(courseId);
        
        // ASSERT
        try {
            assertTrue(result.isPresent(), "Cours devrait être trouvé");
            OK("Course found", false);
            
            assertEquals(courseId, result.get().getId(), "ID du cours devrait correspondre");
            OK("Course ID matches: " + courseId);
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("Comparer des cours devrait retourner les détails des cours demandés")
    void testCompareCourses() {
        // ARRANGE
        List<String> ids = Arrays.asList("IFT2255", "IFT1015");
        Course course1 = new Course("IFT2255", "Génie logiciel");
        Course course2 = new Course("IFT1015", "Programmation I");
        
        when(mockClientApi.get(any(URI.class), eq(Course.class)))
                .thenReturn(course1, course2);
        
        // ACT
        Map<String, Object> result = courseService.compareCourses(ids);
        
        // ASSERT
        try {
            assertNotNull(result, "Resultat ne devrait pas être nul");
            OK("Result is not null", false);
            
            assertTrue(result.containsKey("courses"), "Resultat devrait contenir la clé 'courses'");
            OK("Result contains 'courses' key", false);
            
            @SuppressWarnings("unchecked")
            List<Course> courses = (List<Course>) result.get("courses");
            assertEquals(2, courses.size(), "Devrait y avoir 2 cours dans le resultat");
            OK("Comparison returned 2 courses");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("Devrait retourner un Optional vide pour un cours inexistant")
    void testGetCourseByIdNotExists() {
        // ARRANGE
        String courseId = "IFT9999";
        
        when(mockClientApi.get(any(URI.class), eq(Course.class)))
                .thenThrow(new RuntimeException("Course not found"));
        
        // ACT
        Optional<Course> result = courseService.getCourseById(courseId);
        
        // ASSERT
        try {
            assertTrue(result.isEmpty(), "Devrait retourner un Optional vide");
            OK("Empty Optional returned for non-existent course");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("Devrait retourner des cours filtrés selon les paramètres de requête")
    void testGetAllCoursesWithQueryParams() {
        // ARRANGE
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("session", "A2025");
        queryParams.put("credits", "3");
        
        List<Course> mockCourses = Arrays.asList(
                new Course("IFT2255", "Génie logiciel"));
        
        when(mockClientApi.get(any(URI.class), any(TypeReference.class)))
                .thenReturn(mockCourses);
        
        // ACT
        List<Course> result = courseService.getAllCourses(queryParams);
        
        // ASSERT
        try {
            assertNotNull(result, "Result should not be null");
            OK("Result is not null", false);
            
            assertEquals(1, result.size(), "Should return 1 filtered course");
            OK("Returned 1 filtered course as expected");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    /**************************************************************************
     * Tests for CU#2 - getCoursesInProgram
     *************************************************************************/

    @Test
    @DisplayName("CU#2.1 - Devrait retourner les cours d'un programme avec code valide")
    void testGetCoursesInProgramAvecCodeValide() {
        // ARRANGE
        String programCode = "117510";
        Course course1 = new Course("IFT1015", "Programmation I");
        Course course2 = new Course("IFT2255", "Génie logiciel");
        List<Object> coursesList = Arrays.asList(course1, course2);
        
        Program mockProgram = new Program();
        mockProgram.setCourses(coursesList);
        
        when(mockClientApi.get(any(URI.class), eq(Program.class)))
                .thenReturn(mockProgram);
        
        // ACT
        List<Course> result = courseService.getCoursesInProgram(programCode);
        
        // ASSERT
        try {
            assertNotNull(result, "Résultat ne devrait pas être nul");
            OK("Result is not null", false);
            
            assertFalse(result.isEmpty(), "La liste ne devrait pas être vide");
            OK("List is not empty", false);
            
            assertEquals(2, result.size(), "Devrait y avoir 2 cours");
            OK("Returned 2 courses as expected", false);
            
            assertTrue(result.stream().anyMatch(c -> "IFT1015".equals(c.getId())), 
                    "Devrait contenir IFT1015");
            OK("Contains course IFT1015", false);
            
            assertTrue(result.stream().anyMatch(c -> "IFT2255".equals(c.getId())), 
                    "Devrait contenir IFT2255");
            OK("Contains course IFT2255");
            
            verify(mockClientApi, times(1)).get(any(URI.class), eq(Program.class));
            OK("API called once");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("CU#2.2 - Devrait retourner liste vide si code invalide")
    void testGetCoursesInProgramEchoueSiCodeInvalide() {
        // ARRANGE
        String[] invalidCodes = {"ABC123", "12345", "1234567"};
        
        for (String invalidCode : invalidCodes) {
            // ACT
            List<Course> result = courseService.getCoursesInProgram(invalidCode);
            
            // ASSERT
            try {
                assertNotNull(result, "Résultat ne devrait pas être null");
                OK("Result is not null for code: " + invalidCode, false);
                
                assertTrue(result.isEmpty(), "Liste devrait être vide pour code invalide: " + invalidCode);
                OK("List is empty for invalid code: " + invalidCode, false);
                
                assertEquals(0, result.size(), "Taille devrait être 0");
                OK("Size is 0");
            } catch (AssertionError e) {
                Err(e.getMessage() + " (code: " + invalidCode + ")");
                throw e;
            }
        }
        
        // Vérifier qu'aucun appel API n'a été fait
        verify(mockClientApi, never()).get(any(URI.class), eq(Program.class));
        OK("No API call made for invalid codes");
    }

    @Test
    @DisplayName("CU#2.3 - Devrait retourner liste vide si programme inexistant")
    void testGetCoursesInProgramRetourneListeVideSiProgrammeInexistant() {
        // ARRANGE
        String programCode = "999999";
        
        // Cas 1: Program retourne null
        when(mockClientApi.get(any(URI.class), eq(Program.class)))
                .thenReturn(null);
        
        // ACT
        List<Course> result1 = courseService.getCoursesInProgram(programCode);
        
        // ASSERT
        try {
            assertNotNull(result1, "Résultat ne devrait pas être null");
            OK("Result is not null (null program)", false);
            
            assertTrue(result1.isEmpty(), "Liste devrait être vide");
            OK("List is empty when program is null");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
        
        // Cas 2: Program existe mais sans cours
        Program emptyProgram = new Program();
        emptyProgram.setCourses(Collections.emptyList());
        
        when(mockClientApi.get(any(URI.class), eq(Program.class)))
                .thenReturn(emptyProgram);
        
        // ACT
        List<Course> result2 = courseService.getCoursesInProgram(programCode);
        
        // ASSERT
        try {
            assertNotNull(result2, "Résultat ne devrait pas être null");
            OK("Result is not null (empty program)", false);
            
            assertTrue(result2.isEmpty(), "Liste devrait être vide");
            OK("List is empty when program has no courses");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("CU#2.4 - Devrait gérer erreur API gracieusement")
    void testGetCoursesInProgramGereErreurAPI() {
        // ARRANGE
        String programCode = "117510";
        
        when(mockClientApi.get(any(URI.class), eq(Program.class)))
                .thenThrow(new RuntimeException("API Error"));
        
        // ACT
        List<Course> result = courseService.getCoursesInProgram(programCode);
        
        // ASSERT
        try {
            assertNotNull(result, "Résultat ne devrait pas être null");
            OK("Result is not null", false);
            
            assertTrue(result.isEmpty(), "Liste devrait être vide en cas d'erreur");
            OK("List is empty on API error", false);
            
            assertEquals(0, result.size(), "Taille devrait être 0");
            OK("Size is 0");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    /**************************************************************************
     * Tests for CU#3 - getCoursesByTrimester
     *************************************************************************/

    @Test
    @DisplayName("CU#3.1 - Devrait retourner cours filtrés par trimestre et programme")
    void testGetCoursesByTrimesterAvecTrimestreEtProgrammeValides() {
        // ARRANGE
        String trimester = "H25";
        String programCode = "117510";
        
        Course course1 = new Course("IFT1015", "Programmation I");
        Map<String, Boolean> terms1 = new HashMap<>();
        terms1.put("winter", true);
        terms1.put("autumn", false);
        course1.setAvailable_terms(terms1);
        
        Course course2 = new Course("IFT2255", "Génie logiciel");
        Map<String, Boolean> terms2 = new HashMap<>();
        terms2.put("winter", true);
        terms2.put("autumn", true);
        course2.setAvailable_terms(terms2);
        
        Course course3 = new Course("MAT1400", "Calcul I");
        Map<String, Boolean> terms3 = new HashMap<>();
        terms3.put("winter", false);
        terms3.put("autumn", true);
        course3.setAvailable_terms(terms3);
        
        List<Object> coursesList = Arrays.asList(course1, course2, course3);
        Program mockProgram = new Program();
        mockProgram.setCourses(coursesList);
        
        when(mockClientApi.get(any(URI.class), eq(Program.class)))
                .thenReturn(mockProgram);
        
        // ACT
        List<Course> result = courseService.getCoursesByTrimester(trimester, programCode);
        
        // ASSERT
        try {
            assertNotNull(result, "Résultat ne devrait pas être null");
            OK("Result is not null", false);
            
            assertEquals(2, result.size(), "Devrait retourner 2 cours (seulement ceux avec winter=true)");
            OK("Returned 2 courses as expected", false);
            
            assertTrue(result.stream().anyMatch(c -> "IFT1015".equals(c.getId())), 
                    "Devrait contenir IFT1015");
            OK("Contains IFT1015", false);
            
            assertTrue(result.stream().anyMatch(c -> "IFT2255".equals(c.getId())), 
                    "Devrait contenir IFT2255");
            OK("Contains IFT2255", false);
            
            assertFalse(result.stream().anyMatch(c -> "MAT1400".equals(c.getId())), 
                    "Ne devrait pas contenir MAT1400 (winter=false)");
            OK("Does not contain MAT1400");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("CU#3.2 - Devrait retourner cours filtrés par trimestre sans programme")
    void testGetCoursesByTrimesterSansProgramme() {
        // ARRANGE
        String trimester = "A24";
        String programCode = null;
        
        Course course1 = new Course("IFT1015", "Programmation I");
        Map<String, Boolean> terms1 = new HashMap<>();
        terms1.put("autumn", true);
        course1.setAvailable_terms(terms1);
        
        Course course2 = new Course("IFT2255", "Génie logiciel");
        Map<String, Boolean> terms2 = new HashMap<>();
        terms2.put("autumn", false);
        course2.setAvailable_terms(terms2);
        
        List<Course> allCourses = Arrays.asList(course1, course2);
        
        when(mockClientApi.get(any(URI.class), any(TypeReference.class)))
                .thenReturn(allCourses);
        
        // ACT
        List<Course> result = courseService.getCoursesByTrimester(trimester, programCode);
        
        // ASSERT
        try {
            assertNotNull(result, "Résultat ne devrait pas être null");
            OK("Result is not null", false);
            
            assertEquals(1, result.size(), "Devrait retourner 1 cours (seulement autumn=true)");
            OK("Returned 1 course as expected", false);
            
            assertEquals("IFT1015", result.get(0).getId(), "Devrait contenir IFT1015");
            OK("Contains IFT1015");
            
            verify(mockClientApi, times(1)).get(any(URI.class), any(TypeReference.class));
            OK("API called once with response_level=full");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("CU#3.3 - Devrait retourner liste vide si code trimestre invalide")
    void testGetCoursesByTrimesterEchoueSiCodeTrimestreInvalide() {
        // ARRANGE
        String[] invalidTrimesters = {"INVALID", "X25", "Z99"};
        String programCode = "117510";
        
        for (String invalidTrimester : invalidTrimesters) {
            // ACT
            List<Course> result = courseService.getCoursesByTrimester(invalidTrimester, programCode);
            
            // ASSERT
            try {
                assertNotNull(result, "Résultat ne devrait pas être null");
                OK("Result is not null for: " + invalidTrimester, false);
                
                assertTrue(result.isEmpty(), "Liste devrait être vide");
                OK("List is empty for invalid trimester: " + invalidTrimester);
            } catch (AssertionError e) {
                Err(e.getMessage() + " (trimester: " + invalidTrimester + ")");
                throw e;
            }
        }
        
        // Vérifier qu'aucun appel API n'a été fait
        verify(mockClientApi, never()).get(any(URI.class), any(Class.class));
        verify(mockClientApi, never()).get(any(URI.class), any(TypeReference.class));
        OK("No API call made for invalid trimesters");
    }

    @Test
    @DisplayName("CU#3.4 - Devrait filtrer cours non disponibles pour le trimestre")
    void testGetCoursesByTrimesterFiltreCoursNonDisponibles() {
        // ARRANGE
        String trimester = "E24";
        String programCode = "117510";
        
        // Créer 5 cours, seulement 2 disponibles pour été
        Course course1 = new Course("IFT1015", "Programmation I");
        Map<String, Boolean> terms1 = new HashMap<>();
        terms1.put("summer", true);
        course1.setAvailable_terms(terms1);
        
        Course course2 = new Course("IFT2255", "Génie logiciel");
        Map<String, Boolean> terms2 = new HashMap<>();
        terms2.put("summer", true);
        course2.setAvailable_terms(terms2);
        
        Course course3 = new Course("MAT1400", "Calcul I");
        Map<String, Boolean> terms3 = new HashMap<>();
        terms3.put("summer", false);
        course3.setAvailable_terms(terms3);
        
        Course course4 = new Course("PHY1441", "Physique");
        Map<String, Boolean> terms4 = new HashMap<>();
        terms4.put("summer", null);
        course4.setAvailable_terms(terms4);
        
        Course course5 = new Course("IFT1025", "Programmation II");
        // Pas de available_terms
        
        List<Object> coursesList = Arrays.asList(course1, course2, course3, course4, course5);
        Program mockProgram = new Program();
        mockProgram.setCourses(coursesList);
        
        when(mockClientApi.get(any(URI.class), eq(Program.class)))
                .thenReturn(mockProgram);
        
        // ACT
        List<Course> result = courseService.getCoursesByTrimester(trimester, programCode);
        
        // ASSERT
        try {
            assertNotNull(result, "Résultat ne devrait pas être null");
            OK("Result is not null", false);
            
            assertEquals(2, result.size(), "Devrait retourner exactement 2 cours");
            OK("Returned 2 courses as expected", false);
            
            assertTrue(result.stream().anyMatch(c -> "IFT1015".equals(c.getId())), 
                    "Devrait contenir IFT1015");
            OK("Contains IFT1015", false);
            
            assertTrue(result.stream().anyMatch(c -> "IFT2255".equals(c.getId())), 
                    "Devrait contenir IFT2255");
            OK("Contains IFT2255", false);
            
            assertFalse(result.stream().anyMatch(c -> "MAT1400".equals(c.getId())), 
                    "Ne devrait pas contenir MAT1400 (summer=false)");
            OK("Does not contain MAT1400", false);
            
            assertFalse(result.stream().anyMatch(c -> "PHY1441".equals(c.getId())), 
                    "Ne devrait pas contenir PHY1441 (summer=null)");
            OK("Does not contain PHY1441", false);
            
            assertFalse(result.stream().anyMatch(c -> "IFT1025".equals(c.getId())), 
                    "Ne devrait pas contenir IFT1025 (pas de available_terms)");
            OK("Does not contain IFT1025");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("CU#3.5 - Devrait gérer erreur API gracieusement")
    void testGetCoursesByTrimesterGereErreurAPI() {
        // ARRANGE
        String trimester = "H25";
        String programCode = null;
        
        when(mockClientApi.get(any(URI.class), any(TypeReference.class)))
                .thenThrow(new RuntimeException("API Error"));
        
        // ACT
        List<Course> result = courseService.getCoursesByTrimester(trimester, programCode);
        
        // ASSERT
        try {
            assertNotNull(result, "Résultat ne devrait pas être null");
            OK("Result is not null", false);
            
            assertTrue(result.isEmpty(), "Liste devrait être vide en cas d'erreur");
            OK("List is empty on API error", false);
            
            assertEquals(0, result.size(), "Taille devrait être 0");
            OK("Size is 0");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    /**************************************************************************
     * Tests for CU#4 - getCourseSchedule
     *************************************************************************/

    @Test
    @DisplayName("CU#4.1 - Devrait retourner horaire avec paramètres valides")
    void testGetCourseScheduleAvecParametresValides() {
        // ARRANGE
        String courseId = "IFT2255";
        String semester = "A25";
        
        String jsonResponse = """
            {
                "schedules": [{
                    "sections": [{
                        "name": "A01",
                        "volets": [{
                            "name": "Cours",
                            "activities": [{
                                "days": ["LU"],
                                "start_time": "09:00",
                                "end_time": "12:00",
                                "room": "PK-1234"
                            }]
                        }]
                    }]
                }]
            }
            """;
        
        HttpClientApiResponse mockResponse = new HttpClientApiResponse(200, "OK", jsonResponse);
        
        when(mockClientApi.get(any(URI.class)))
                .thenReturn(mockResponse);
        
        // ACT
        CourseScheduleResponse result = courseService.getCourseSchedule(courseId, semester);
        
        // ASSERT
        try {
            assertNotNull(result, "Résultat ne devrait pas être null");
            OK("Result is not null", false);
            
            assertEquals("IFT2255", result.courseId, "courseId devrait être normalisé");
            OK("Course ID normalized: " + result.courseId, false);
            
            assertEquals("A25", result.semester, "semester devrait être normalisé");
            OK("Semester normalized: " + result.semester, false);
            
            assertNotNull(result.sections, "sections ne devrait pas être null");
            OK("Sections is not null", false);
            
            assertFalse(result.sections.isEmpty(), "sections ne devrait pas être vide");
            OK("Sections is not empty", false);
            
            assertEquals(1, result.sections.size(), "Devrait avoir 1 section");
            OK("Has 1 section", false);
            
            assertEquals("A01", result.sections.get(0).sectionCode, "Section code devrait être A01");
            OK("Section code is A01");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("CU#4.2 - Devrait retourner null si cours inexistant")
    void testGetCourseScheduleEchoueSiCoursInexistant() {
        // ARRANGE
        String courseId = "IFT9999";
        String semester = "H25";
        
        // Cas 1: Statut HTTP >= 300
        HttpClientApiResponse errorResponse = new HttpClientApiResponse(404, "Not Found", "");
        
        when(mockClientApi.get(any(URI.class)))
                .thenReturn(errorResponse);
        
        // ACT
        CourseScheduleResponse result1 = courseService.getCourseSchedule(courseId, semester);
        
        // ASSERT
        try {
            assertNull(result1, "Résultat devrait être null pour statut HTTP invalide");
            OK("Result is null for HTTP status >= 300");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
        
        // Cas 2: Body null
        HttpClientApiResponse nullBodyResponse = new HttpClientApiResponse(200, "OK", null);
        
        when(mockClientApi.get(any(URI.class)))
                .thenReturn(nullBodyResponse);
        
        // ACT
        CourseScheduleResponse result2 = courseService.getCourseSchedule(courseId, semester);
        
        // ASSERT
        try {
            assertNull(result2, "Résultat devrait être null pour body null");
            OK("Result is null for null body");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("CU#4.3 - Devrait retourner null si paramètres vides")
    void testGetCourseScheduleEchoueSiParametresVides() {
        // ARRANGE & ACT & ASSERT
        String[] emptyCourseIds = {null, "", "   "};
        String[] emptySemesters = {null, "", "   "};
        
        for (String courseId : emptyCourseIds) {
            CourseScheduleResponse result = courseService.getCourseSchedule(courseId, "A25");
            try {
                assertNull(result, "Résultat devrait être null pour courseId: " + courseId);
                OK("Result is null for empty courseId: " + courseId);
            } catch (AssertionError e) {
                Err(e.getMessage());
                throw e;
            }
        }
        
        for (String semester : emptySemesters) {
            CourseScheduleResponse result = courseService.getCourseSchedule("IFT2255", semester);
            try {
                assertNull(result, "Résultat devrait être null pour semester: " + semester);
                OK("Result is null for empty semester: " + semester);
            } catch (AssertionError e) {
                Err(e.getMessage());
                throw e;
            }
        }
        
        // Vérifier qu'aucun appel API n'a été fait
        verify(mockClientApi, never()).get(any(URI.class));
        OK("No API call made for empty parameters");
    }

    @Test
    @DisplayName("CU#4.4 - Devrait retourner structure vide si pas de sections")
    void testGetCourseScheduleRetourneStructureVideSiPasDeSections() {
        // ARRANGE
        String courseId = "IFT1015";
        String semester = "E24";
        
        // JSON valide mais sans sections
        String jsonResponse = """
            {
                "schedules": []
            }
            """;
        
        HttpClientApiResponse mockResponse = new HttpClientApiResponse(200, "OK", jsonResponse);
        
        when(mockClientApi.get(any(URI.class)))
                .thenReturn(mockResponse);
        
        // ACT
        CourseScheduleResponse result = courseService.getCourseSchedule(courseId, semester);
        
        // ASSERT
        try {
            assertNotNull(result, "Résultat ne devrait pas être null");
            OK("Result is not null", false);
            
            assertEquals("IFT1015", result.courseId, "courseId devrait être correct");
            OK("Course ID is correct", false);
            
            assertEquals("E24", result.semester, "semester devrait être correct");
            OK("Semester is correct", false);
            
            assertNotNull(result.sections, "sections ne devrait pas être null");
            OK("Sections is not null", false);
            
            assertTrue(result.sections.isEmpty(), "sections devrait être vide");
            OK("Sections is empty");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("CU#4.5 - Devrait parser structure complexe complète")
    void testGetCourseScheduleParseStructureComplexe() {
        // ARRANGE
        String courseId = "IFT2255";
        String semester = "A25";
        
        String jsonResponse = """
            {
                "schedules": [{
                    "sections": [{
                        "name": "A01",
                        "volets": [{
                            "name": "Cours",
                            "activities": [{
                                "days": ["LU"],
                                "start_time": "09:00",
                                "end_time": "12:00",
                                "room": "PK-1234"
                            }]
                        }, {
                            "name": "Laboratoire",
                            "activities": [{
                                "days": ["ME"],
                                "start_time": "14:00",
                                "end_time": "17:00",
                                "room": "PK-5678"
                            }]
                        }]
                    }, {
                        "name": "A02",
                        "volets": [{
                            "name": "Cours",
                            "activities": [{
                                "days": ["MA"],
                                "start_time": "10:00",
                                "end_time": "13:00",
                                "room": "PK-9999"
                            }]
                        }]
                    }]
                }]
            }
            """;
        
        HttpClientApiResponse mockResponse = new HttpClientApiResponse(200, "OK", jsonResponse);
        
        when(mockClientApi.get(any(URI.class)))
                .thenReturn(mockResponse);
        
        // ACT
        CourseScheduleResponse result = courseService.getCourseSchedule(courseId, semester);
        
        // ASSERT
        try {
            assertNotNull(result, "Résultat ne devrait pas être null");
            OK("Result is not null", false);
            
            assertEquals(2, result.sections.size(), "Devrait avoir 2 sections");
            OK("Has 2 sections", false);
            
            // Vérifier section A01
            assertEquals("A01", result.sections.get(0).sectionCode, "Première section devrait être A01");
            OK("First section is A01", false);
            
            assertEquals(2, result.sections.get(0).activities.size(), 
                    "Section A01 devrait avoir 2 activités");
            OK("Section A01 has 2 activities", false);
            
            // Vérifier conversion des jours
            assertTrue(result.sections.get(0).activities.get(0).entries.stream()
                    .anyMatch(e -> "Lundi".equals(e.day)), 
                    "Jour devrait être converti en 'Lundi'");
            OK("Day converted to 'Lundi'", false);
            
            assertTrue(result.sections.get(0).activities.get(1).entries.stream()
                    .anyMatch(e -> "Mercredi".equals(e.day)), 
                    "Jour devrait être converti en 'Mercredi'");
            OK("Day converted to 'Mercredi'", false);
            
            // Vérifier section A02
            assertEquals("A02", result.sections.get(1).sectionCode, "Deuxième section devrait être A02");
            OK("Second section is A02", false);
            
            assertTrue(result.sections.get(1).activities.get(0).entries.stream()
                    .anyMatch(e -> "Mardi".equals(e.day)), 
                    "Jour devrait être converti en 'Mardi'");
            OK("Day converted to 'Mardi'");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("CU#4.6 - Devrait gérer erreur parsing JSON gracieusement")
    void testGetCourseScheduleGereErreurParsingJSON() {
        // ARRANGE
        String courseId = "IFT2255";
        String semester = "H25";
        
        // JSON malformé
        String malformedJson = "{ invalid json }";
        
        HttpClientApiResponse mockResponse = new HttpClientApiResponse(200, "OK", malformedJson);
        
        when(mockClientApi.get(any(URI.class)))
                .thenReturn(mockResponse);
        
        // ACT
        CourseScheduleResponse result = courseService.getCourseSchedule(courseId, semester);
        
        // ASSERT
        try {
            assertNotNull(result, "Résultat ne devrait pas être null");
            OK("Result is not null", false);
            
            assertEquals("IFT2255", result.courseId, "courseId devrait être correct");
            OK("Course ID is correct", false);
            
            assertEquals("H25", result.semester, "semester devrait être correct");
            OK("Semester is correct", false);
            
            assertNotNull(result.sections, "sections ne devrait pas être null");
            OK("Sections is not null", false);
            
            assertTrue(result.sections.isEmpty(), 
                    "sections devrait être vide en cas d'erreur parsing");
            OK("Sections is empty on parsing error");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @AfterAll
    static void printFooter() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("COMPLETED: CourseService Tests");
        System.out.println("=".repeat(80) + "\n");
    }

    // Helper methods
    private void printMessage(String message, boolean isOk, boolean isLast) {
        String symbol = isLast ? "└─" : "├─";
        String status = isOk ? "[PASS]" : "[FAIL]";
        System.out.println("    │   " + symbol + " " + status + " " + message);
    }

    private void OK(String message) {
        printMessage(message, true, true);
    }

    private void OK(String message, boolean isLast) {
        printMessage(message, true, isLast);
    }

    private void Err(String message) {
        printMessage(message, false, true);
    }
}
