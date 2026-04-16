package com.diro.ift2255.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;

import com.diro.ift2255.model.Review;

public class ReviewServiceTest {
    private ReviewService reviewService;
    private long testStartTime;

    @BeforeAll
    static void printHeader() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("ReviewService Tests");
        System.out.println("=".repeat(80));
    }

    @BeforeEach
    void setup(TestInfo testInfo) {
        reviewService = new ReviewService();
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
    @DisplayName("Devrait retourner les avis pour un cours existant")
    void testGetReviewsForCourseExists() {
        // ARRANGE
        String courseId = "IFT2255";
        
        // ACT
        List<Review> reviews = reviewService.getReviews(courseId);
        
        // ASSERT
        try {
            assertNotNull(reviews, "Liste des avis ne devrait pas être nulle");
            OK("Reviews list is not null", false);
            
            assertTrue(reviews.size() >= 2, "Devrait y avoir au moins 2 avis pour ce cours");
            OK("Found " + reviews.size() + " reviews for " + courseId, false);
            
            assertTrue(reviews.stream().allMatch(r -> r.getCourseId().equalsIgnoreCase(courseId)),
                    "Tous les avis devraient correspondre à l'ID du cours");
            OK("All reviews match the course ID");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("Ne devrait retourner aucun avis pour un cours inexistant")
    void testGetReviewsForCourseNotExists() {
        // ARRANGE
        String courseId = "IFT9999";
        
        // ACT
        List<Review> reviews = reviewService.getReviews(courseId);
        
        // ASSERT
        try {
            assertNotNull(reviews, "Liste des avis ne devrait pas être nulle");
            OK("Reviews list is not null", false);
            
            assertTrue(reviews.isEmpty(), "Ne devrait y avoir aucun avis pour cours inexistant");
            OK("No reviews found for " + courseId + " as expected");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("Ajouter un avis devrait augmenter le nombre total d'avis pour le cours")
    void testAddReview() {
        // ARRANGE
        int initialSize = reviewService.getReviews("IFT2255").size();
        Review newReview = new Review("IFT2255", "testStudent", 6, "Test comment");
        
        // ACT
        reviewService.addReview(newReview);
        List<Review> updatedReviews = reviewService.getReviews("IFT2255");
        
        // ASSERT
        try {
            assertEquals(initialSize + 1, updatedReviews.size(), 
                    "Nombre total d'avis devrait augmenter de 1");
            OK("Review count increased from " + initialSize + " to " + updatedReviews.size(), false);
            
            assertTrue(updatedReviews.stream()
                    .anyMatch(r -> r.getAuthor().equals("testStudent")),
                    "Nouvel avis devrait être dans la liste");
            OK("New review found in the list");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("Get reviews should return all reviews with correct structure")
    void testGetReviewsStructure() {
        // ARRANGE
        String courseId = "IFT2255";
        
        // ACT
        List<Review> reviews = reviewService.getReviews(courseId);
        
        // ASSERT
        try {
            assertNotNull(reviews, "Reviews list should not be null");
            OK("Reviews list is not null", false);
            
            assertTrue(reviews.size() >= 1, "Should have at least one review");
            OK("Found " + reviews.size() + " reviews for " + courseId, false);
            
            // Verify structure of first review
            if (!reviews.isEmpty()) {
                Review firstReview = reviews.get(0);
                assertNotNull(firstReview.getCourseId(), "CourseId should not be null");
                assertNotNull(firstReview.getAuthor(), "Author should not be null");
                assertTrue(firstReview.getDifficulty() >= 1 && firstReview.getDifficulty() <= 10, 
                        "Difficulty should be between 1 and 10");
                OK("Review structure is valid");
            }
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }
    @Test
    @DisplayName("Ajouter plusieurs avis devrait fonctionner correctement")
    void testAddMultipleReviews() {
        // ARRANGE
        String courseId = "IFT3150";
        int initialSize = reviewService.getReviews(courseId).size();
        
        Review review1 = new Review(courseId, "student1", 7, "Good course");
        Review review2 = new Review(courseId, "student2", 8, "Very interesting");
        Review review3 = new Review(courseId, "student3", 6, "Challenging");
        
        // ACT
        reviewService.addReview(review1);
        reviewService.addReview(review2);
        reviewService.addReview(review3);
        
        List<Review> updatedReviews = reviewService.getReviews(courseId);
        
        // ASSERT
        try {
            assertEquals(initialSize + 3, updatedReviews.size(), 
                    "Nombre total d'avis devrait augmenter de 3");
            OK("Review count increased from " + initialSize + " to " + updatedReviews.size(), false);
            
            assertTrue(updatedReviews.stream()
                    .anyMatch(r -> r.getAuthor().equals("student1")),
                    "1er avis devrait être présent");
            OK("First review found", false);
            
            assertTrue(updatedReviews.stream()
                    .anyMatch(r -> r.getAuthor().equals("student2")),
                    "2e avis devrait être présent");
            OK("Second review found", false);
            
            assertTrue(updatedReviews.stream()
                    .anyMatch(r -> r.getAuthor().equals("student3")),
                    "3e avis devrait être présent");
            OK("Third review found");
        } catch (AssertionError e) {
            Err(e.getMessage());
            throw e;
        }
    }

    @AfterAll
    static void printFooter() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("COMPLETED: ReviewService Tests");
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