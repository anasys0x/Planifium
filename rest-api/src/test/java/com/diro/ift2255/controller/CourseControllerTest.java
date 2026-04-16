package com.diro.ift2255.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.diro.ift2255.model.Course;
import com.diro.ift2255.service.CourseService;

import io.javalin.http.Context;

@ExtendWith(MockitoExtension.class) // ← Active Mockito pour ce test
public class CourseControllerTest {

    @Mock // ← Crée un FAUX CourseService
    private CourseService mockService;

    @Mock // ← Crée un FAUX Context Javalin
    private Context mockContext;

    private CourseController controller; // ← Le VRAI contrôleur à tester

    private long testStartTime;

    @BeforeAll
    static void printHeader() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CourseController Tests");
        System.out.println("=".repeat(80));
    }

    @BeforeEach
    void setup(TestInfo testInfo) {
        // On injecte les FAUX objets dans le VRAI contrôleur
        controller = new CourseController(mockService);
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

    /**************************************************************************
     * Tests for getAllCourses method
     *************************************************************************/

    @Test
    @DisplayName("Get all courses should return all courses when no query parameters")
    void testGetAllCoursesWithoutQueryParams() {
        // ARRANGE
        List<Course> mockCourses = Arrays.asList(
                new Course("IFT1015", "Programmation I"),
                new Course("IFT1025", "Programmation II"));

        when(mockContext.queryParamMap()).thenReturn(new HashMap<>());
        when(mockService.getAllCourses(any())).thenReturn(mockCourses);

        // ACT
        controller.getAllCourses(mockContext);

        // ASSERT
        try {
            verify(mockContext).queryParamMap();
            OK("Query params extracted from context", false);

            verify(mockService).getAllCourses(any(Map.class));
            OK("Service called with query params", false);

            verify(mockContext).json(mockCourses);
            OK("Response returned with " + mockCourses.size() + " courses");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("Get all courses should pass query parameters to service")
    void testGetAllCoursesWithQueryParameters() {
        // ARRANGE
        Map<String, List<String>> queryParamMap = new HashMap<>();
        queryParamMap.put("session", Arrays.asList("A2025"));

        List<Course> mockCourses = Arrays.asList(
                new Course("IFT1015", "Programmation I"));

        when(mockContext.queryParamMap()).thenReturn(queryParamMap);
        when(mockService.getAllCourses(any())).thenReturn(mockCourses);

        // ACT
        controller.getAllCourses(mockContext);

        // ASSERT
        try {
            verify(mockService).getAllCourses(argThat(params -> 
                    params.containsKey("session") &&
                    params.get("session").equals("A2025")));
            OK("Service called with correct query parameters", false);

            verify(mockContext).json(mockCourses);
            OK("Response returned successfully");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    /**************************************************************************
     * Tests for getCourseById method
     *************************************************************************/

    @Test
    @DisplayName("Get course by ID should return course when ID exists")
    void testGetCourseByIdWhenIdExists() {
        // ARRANGE
        String courseId = "IFT2255";
        Course mockCourse = new Course(courseId, "Génie logiciel");

        when(mockContext.pathParam("id")).thenReturn(courseId);
        when(mockService.getCourseById(courseId)).thenReturn(Optional.of(mockCourse));

        // ACT
        controller.getCourseById(mockContext);

        // ASSERT
        try {
            verify(mockContext).pathParam("id");
            OK("Path parameter 'id' extracted", false);

            verify(mockService).getCourseById(courseId);
            OK("Service called with ID: " + courseId, false);

            verify(mockContext).json(mockCourse);
            OK("Course returned successfully", false);

            verify(mockContext, never()).status(anyInt());
            OK("No error status set");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("Get course by ID should return 404 when course not found")
    void testGetCourseByIdWhenCourseNotFound() {
        // ARRANGE
        String courseId = "IFT1234";

        when(mockContext.pathParam("id")).thenReturn(courseId);
        when(mockService.getCourseById(courseId)).thenReturn(Optional.empty());
        when(mockContext.status(404)).thenReturn(mockContext);

        // ACT
        controller.getCourseById(mockContext);

        // ASSERT
        try {
            verify(mockService).getCourseById(courseId);
            OK("Service called with ID: " + courseId, false);

            verify(mockContext).status(404);
            OK("Status 404 set", false);

            verify(mockContext).json(argThat(map -> map instanceof Map &&
                    ((Map<?, ?>) map).containsKey("error")));
            OK("Error message returned");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("Get course by ID should return 400 when ID is null")
    void testGetCourseByIdWhenIdIsNull() {
        // ARRANGE
        when(mockContext.pathParam("id")).thenReturn(null);
        when(mockContext.status(400)).thenReturn(mockContext);

        // ACT
        controller.getCourseById(mockContext);

        // ASSERT
        try {
            verify(mockContext).status(400);
            OK("Status 400 set", false);

            verify(mockContext).json(argThat(map -> map instanceof Map &&
                    ((Map<?, ?>) map).containsKey("error")));
            OK("Error message returned", false);

            verify(mockService, never()).getCourseById(any());
            OK("Service was not called");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("Get course by ID should return 400 when ID is empty string")
    void testGetCourseByIdWhenIdIsEmpty() {
        // ARRANGE
        when(mockContext.pathParam("id")).thenReturn("");
        when(mockContext.status(400)).thenReturn(mockContext);

        // ACT
        controller.getCourseById(mockContext);

        // ASSERT
        try {
            verify(mockContext).status(400);
            OK("Status 400 set for empty ID", false);

            verify(mockService, never()).getCourseById(any());
            OK("Service was not called");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
@DisplayName("Compare courses should return multiple courses by IDs")
void testCompareCourses() {
    // ARRANGE
    List<String> courseIds = Arrays.asList("IFT2255", "IFT1015");
    
    Course course1 = new Course("IFT2255", "Génie logiciel");
    Course course2 = new Course("IFT1015", "Programmation I");
    
    Map<String, Object> expectedResult = new HashMap<>();
    expectedResult.put("courses", Arrays.asList(course1, course2));
    
    when(mockContext.queryParams("id")).thenReturn(courseIds);
    when(mockService.compareCourses(courseIds)).thenReturn(expectedResult);
    
    // ACT
    controller.compareCourses(mockContext);
    
    // ASSERT
    try {
        verify(mockContext).queryParams("id");
        OK("Query params 'id' extracted", false);
        
        verify(mockService).compareCourses(courseIds);
        OK("Service called with course IDs", false);
        
        verify(mockContext).json(expectedResult);
        OK("Comparison result returned with 2 courses");
    } catch (AssertionError e) {
        Err(e.getMessage());
        throw e;
    }
}

    /**************************************************************************
     * Tests for getCoursesInProgram method
     *************************************************************************/

    @Test
    @DisplayName("Get courses in program should return courses when program exists")
    void testGetCoursesInProgramWhenProgramExists() {
        // ARRANGE
        String programCode = "117510";
        List<Course> mockCourses = Arrays.asList(
                new Course("IFT1015", "Programmation I"),
                new Course("IFT1025", "Programmation II"));

        when(mockContext.queryParam("program")).thenReturn(programCode);
        when(mockService.getCoursesInProgram(programCode)).thenReturn(mockCourses);

        // ACT
        controller.getCoursesInProgram(mockContext);

        // ASSERT
        try {
            verify(mockContext).queryParam("program");
            OK("Query parameter 'program' extracted", false);

            verify(mockService).getCoursesInProgram(programCode);
            OK("Service called with program code: " + programCode, false);

            verify(mockContext).json(mockCourses);
            OK("Response returned with " + mockCourses.size() + " courses", false);

            verify(mockContext, never()).status(anyInt());
            OK("No error status set");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("Get courses in program should return 422 when program parameter is missing")
    void testGetCoursesInProgramWhenProgramIsNull() {
        // ARRANGE
        when(mockContext.queryParam("program")).thenReturn(null);
        when(mockContext.status(422)).thenReturn(mockContext);

        // ACT
        controller.getCoursesInProgram(mockContext);

        // ASSERT
        try {
            verify(mockContext).queryParam("program");
            OK("Query parameter 'program' extracted", false);

            verify(mockContext).status(422);
            OK("Status 422 set", false);

            verify(mockContext).json(argThat(map -> map instanceof Map &&
                    ((Map<?, ?>) map).containsKey("error")));
            OK("Error message returned", false);

            verify(mockService, never()).getCoursesInProgram(any());
            OK("Service was not called");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("Get courses in program should return 422 when program parameter is blank")
    void testGetCoursesInProgramWhenProgramIsBlank() {
        // ARRANGE
        when(mockContext.queryParam("program")).thenReturn("   ");
        when(mockContext.status(422)).thenReturn(mockContext);

        // ACT
        controller.getCoursesInProgram(mockContext);

        // ASSERT
        try {
            verify(mockContext).queryParam("program");
            OK("Query parameter 'program' extracted", false);

            verify(mockContext).status(422);
            OK("Status 422 set for blank program", false);

            verify(mockContext).json(argThat(map -> map instanceof Map &&
                    ((Map<?, ?>) map).containsKey("error")));
            OK("Error message returned", false);

            verify(mockService, never()).getCoursesInProgram(any());
            OK("Service was not called");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("Get courses in program should return 404 when no courses found")
    void testGetCoursesInProgramWhenNoCoursesFound() {
        // ARRANGE
        String programCode = "117510";

        when(mockContext.queryParam("program")).thenReturn(programCode);
        when(mockService.getCoursesInProgram(programCode)).thenReturn(Collections.emptyList());
        when(mockContext.status(404)).thenReturn(mockContext);

        // ACT
        controller.getCoursesInProgram(mockContext);

        // ASSERT
        try {
            verify(mockContext).queryParam("program");
            OK("Query parameter 'program' extracted", false);

            verify(mockService).getCoursesInProgram(programCode);
            OK("Service called with program code: " + programCode, false);

            verify(mockContext).status(404);
            OK("Status 404 set", false);

            verify(mockContext).json(argThat(map -> map instanceof Map &&
                    ((Map<?, ?>) map).containsKey("error")));
            OK("Error message returned");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    /**************************************************************************
     * Tests for getCoursesBySemester method
     *************************************************************************/

    @Test
    @DisplayName("Get courses by semester should return courses when semester exists")
    void testGetCoursesBySemesterWhenSemesterExists() {
        // ARRANGE
        String semester = "H25";
        List<Course> mockCourses = Arrays.asList(
                new Course("IFT1015", "Programmation I"),
                new Course("IFT1025", "Programmation II"));

        when(mockContext.pathParam("code")).thenReturn(semester);
        when(mockContext.queryParam("program")).thenReturn(null);
        when(mockService.getCoursesByTrimester(semester, null)).thenReturn(mockCourses);

        // ACT
        controller.getCoursesBySemester(mockContext);

        // ASSERT
        try {
            verify(mockContext).pathParam("code");
            OK("Path parameter 'code' extracted", false);

            verify(mockContext).queryParam("program");
            OK("Query parameter 'program' checked", false);

            verify(mockService).getCoursesByTrimester(semester, null);
            OK("Service called with semester: " + semester, false);

            verify(mockContext).json(mockCourses);
            OK("Response returned with " + mockCourses.size() + " courses", false);

            verify(mockContext, never()).status(anyInt());
            OK("No error status set");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("Get courses by semester should return courses when semester and program exist")
    void testGetCoursesBySemesterWhenSemesterAndProgramExist() {
        // ARRANGE
        String semester = "A24";
        String programCode = "117510";
        List<Course> mockCourses = Arrays.asList(
                new Course("IFT1015", "Programmation I"));

        when(mockContext.pathParam("code")).thenReturn(semester);
        when(mockContext.queryParam("program")).thenReturn(programCode);
        when(mockService.getCoursesByTrimester(semester, programCode)).thenReturn(mockCourses);

        // ACT
        controller.getCoursesBySemester(mockContext);

        // ASSERT
        try {
            verify(mockContext).pathParam("code");
            OK("Path parameter 'code' extracted", false);

            verify(mockContext).queryParam("program");
            OK("Query parameter 'program' extracted", false);

            verify(mockService).getCoursesByTrimester(semester, programCode);
            OK("Service called with semester: " + semester + " and program: " + programCode, false);

            verify(mockContext).json(mockCourses);
            OK("Response returned with " + mockCourses.size() + " courses", false);

            verify(mockContext, never()).status(anyInt());
            OK("No error status set");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("Get courses by semester should return 400 when semester format is invalid")
    void testGetCoursesBySemesterWhenSemesterFormatIsInvalid() {
        // ARRANGE
        String invalidSemester = "INVALID";

        when(mockContext.pathParam("code")).thenReturn(invalidSemester);
        when(mockContext.status(400)).thenReturn(mockContext);

        // ACT
        controller.getCoursesBySemester(mockContext);

        // ASSERT
        try {
            verify(mockContext).pathParam("code");
            OK("Path parameter 'code' extracted", false);

            verify(mockContext).status(400);
            OK("Status 400 set", false);

            verify(mockContext).json(argThat(map -> map instanceof Map &&
                    ((Map<?, ?>) map).containsKey("error")));
            OK("Error message returned", false);

            verify(mockService, never()).getCoursesByTrimester(anyString(), any());
            OK("Service was not called");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("Get courses by semester should return 404 when no courses found")
    void testGetCoursesBySemesterWhenNoCoursesFound() {
        // ARRANGE
        String semester = "E24";

        when(mockContext.pathParam("code")).thenReturn(semester);
        when(mockContext.queryParam("program")).thenReturn(null);
        when(mockService.getCoursesByTrimester(semester, null)).thenReturn(Collections.emptyList());
        when(mockContext.status(404)).thenReturn(mockContext);

        // ACT
        controller.getCoursesBySemester(mockContext);

        // ASSERT
        try {
            verify(mockContext).pathParam("code");
            OK("Path parameter 'code' extracted", false);

            verify(mockService).getCoursesByTrimester(semester, null);
            OK("Service called with semester: " + semester, false);

            verify(mockContext).status(404);
            OK("Status 404 set", false);

            verify(mockContext).json(argThat(map -> map instanceof Map &&
                    ((Map<?, ?>) map).containsKey("error")));
            OK("Error message returned");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("Get courses by semester should return 404 when no courses found for program")
    void testGetCoursesBySemesterWhenNoCoursesFoundForProgram() {
        // ARRANGE
        String semester = "H25";
        String programCode = "117510";

        when(mockContext.pathParam("code")).thenReturn(semester);
        when(mockContext.queryParam("program")).thenReturn(programCode);
        when(mockService.getCoursesByTrimester(semester, programCode)).thenReturn(Collections.emptyList());
        when(mockContext.status(404)).thenReturn(mockContext);

        // ACT
        controller.getCoursesBySemester(mockContext);

        // ASSERT
        try {
            verify(mockContext).pathParam("code");
            OK("Path parameter 'code' extracted", false);

            verify(mockContext).queryParam("program");
            OK("Query parameter 'program' extracted", false);

            verify(mockService).getCoursesByTrimester(semester, programCode);
            OK("Service called with semester: " + semester + " and program: " + programCode, false);

            verify(mockContext).status(404);
            OK("Status 404 set", false);

            verify(mockContext).json(argThat(map -> map instanceof Map &&
                    ((Map<?, ?>) map).containsKey("error")));
            OK("Error message returned");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("Get courses by semester should return 500 when exception is thrown")
    void testGetCoursesBySemesterWhenExceptionThrown() {
        // ARRANGE
        String semester = "A24";

        when(mockContext.pathParam("code")).thenReturn(semester);
        when(mockContext.queryParam("program")).thenReturn(null);
        when(mockService.getCoursesByTrimester(semester, null))
                .thenThrow(new RuntimeException("Service error"));
        when(mockContext.status(500)).thenReturn(mockContext);

        // ACT
        controller.getCoursesBySemester(mockContext);

        // ASSERT
        try {
            verify(mockContext).pathParam("code");
            OK("Path parameter 'code' extracted", false);

            verify(mockService).getCoursesByTrimester(semester, null);
            OK("Service called with semester: " + semester, false);

            verify(mockContext).status(500);
            OK("Status 500 set", false);

            verify(mockContext).json(argThat(map -> map instanceof Map &&
                    ((Map<?, ?>) map).containsKey("error")));
            OK("Error message returned");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @AfterAll
    static void printFooter() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("COMPLETED: CourseController Tests");
        System.out.println("=".repeat(80) + "\n");
    }

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

    private void Err(String message, boolean isLast) {
        printMessage(message, false, isLast);
    }
}
