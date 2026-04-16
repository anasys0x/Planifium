package com.diro.ift2255.service;

import com.diro.ift2255.model.Course;
import com.diro.ift2255.model.dto.CheckEligibilityResponse;
import com.diro.ift2255.util.HttpClientApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la fonctionnalité "Vérifier l'éligibilité à un cours".
 *
 * Pré-requis:
 * - CourseService doit avoir une méthode:
 *   CheckEligibilityResponse checkEligibility(String courseId, List<String> completedCourses, int cycle)
 *
 * Dépendance mockée:
 * - HttpClientApi (Planifium)
 */
@ExtendWith(MockitoExtension.class)
public class CourseServiceEligibilityTest {

    @Mock
    private HttpClientApi clientApi;

    private CourseService courseService;

    @BeforeEach
    void setup() {
        courseService = new CourseService(clientApi);
    }

    @Test
    void eligible_whenPrereqsSatisfied_andCycleOk() {
        // Arrange
        Course c = new Course();
        c.setId("IFT2255");
        c.setPrerequisite_courses(List.of("IFT1025", "IFT1065"));

        when(clientApi.get(ArgumentMatchers.any(URI.class), eq(Course.class)))
                .thenReturn(c);

        // Act
        CheckEligibilityResponse res = courseService.checkEligibility(
                "IFT2255",
                List.of("IFT1025", "IFT1065", "IFT2015"),
                1
        );

        // Assert
        assertTrue(res.eligible);
        assertTrue(res.cycle_ok);
        assertNotNull(res.missing_prerequisites);
        assertTrue(res.missing_prerequisites.isEmpty());

        // Effet de bord attendu : 1 appel Planifium
        verify(clientApi, times(1)).get(any(URI.class), eq(Course.class));
    }

    @Test
    void notEligible_whenMissingPrerequisites() {
        // Arrange
        Course c = new Course();
        c.setId("IFT2255");
        c.setPrerequisite_courses(List.of("IFT1025", "IFT1065"));

        when(clientApi.get(any(URI.class), eq(Course.class)))
                .thenReturn(c);

        // Act
        CheckEligibilityResponse res = courseService.checkEligibility(
                "IFT2255",
                List.of("IFT1025"), // manque IFT1065
                1
        );

        // Assert
        assertFalse(res.eligible);
        assertTrue(res.cycle_ok); // cycle ok
        assertEquals(List.of("IFT1065"), res.missing_prerequisites);

        verify(clientApi, times(1)).get(any(URI.class), eq(Course.class));
    }

    @Test
    void notEligible_whenCycleNotAppropriate_evenIfPrereqsOk() {
        // Arrange
        // cours "5001" => considéré cycle 2/3 selon ta règle (>=5000)
        Course c = new Course();
        c.setId("IFT5001");
        c.setPrerequisite_courses(List.of("IFT1025"));

        when(clientApi.get(any(URI.class), eq(Course.class)))
                .thenReturn(c);

        // Act
        CheckEligibilityResponse res = courseService.checkEligibility(
                "IFT5001",
                List.of("IFT1025"),
                1 // cycle 1 => doit être refusé
        );

        // Assert
        assertFalse(res.eligible);
        assertFalse(res.cycle_ok);
        assertTrue(res.missing_prerequisites.isEmpty());

        verify(clientApi, times(1)).get(any(URI.class), eq(Course.class));
    }

    @Test
    void caseInsensitive_completedCourses_shouldMatchPrereqs() {
        // Arrange
        Course c = new Course();
        c.setId("IFT2255");
        c.setPrerequisite_courses(List.of("IFT1025", "IFT1065"));

        when(clientApi.get(any(URI.class), eq(Course.class)))
                .thenReturn(c);

        // Act
        CheckEligibilityResponse res = courseService.checkEligibility(
                "ift2255",                      // courseId en minuscules
                List.of("ift1025", "ift1065"),   // prereqs en minuscules
                1
        );

        // Assert
        assertTrue(res.eligible);
        assertTrue(res.cycle_ok);
        assertTrue(res.missing_prerequisites.isEmpty());

        verify(clientApi, times(1)).get(any(URI.class), eq(Course.class));
    }

    @Test
    void courseNotFound_shouldReturnNonEligible_andFriendlyMessageOrFlag() {
        // Arrange
        // Simule Planifium qui échoue -> getCourseById retourne Optional.empty()
        when(clientApi.get(any(URI.class), eq(Course.class)))
                .thenThrow(new RuntimeException("404"));

        // Act
        CheckEligibilityResponse res = courseService.checkEligibility(
                "IFT9999",
                List.of("IFT1025"),
                1
        );

        // Assert
        // Selon ton implémentation, tu peux soit:
        // - retourner eligible=false avec message "Cours introuvable"
        // - OU lever une exception custom
        // Ici on suppose la version "retour objet + message"
        assertFalse(res.eligible);
        assertNotNull(res.message);
        assertTrue(res.message.toLowerCase().contains("introuvable"));

        verify(clientApi, times(1)).get(any(URI.class), eq(Course.class));
    }
}
