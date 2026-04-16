package com.diro.ift2255.config;

import com.diro.ift2255.controller.CourseController;
import com.diro.ift2255.controller.ReviewController;
import com.diro.ift2255.controller.UserController;
import com.diro.ift2255.repository.ResultsRepository;
import com.diro.ift2255.service.CourseService;
import com.diro.ift2255.service.ResultsService;
import com.diro.ift2255.service.ReviewService;
import com.diro.ift2255.service.UserService;
import com.diro.ift2255.util.HttpClientApi;

import io.javalin.Javalin;

public class Routes {

    public static void register(Javalin app, ReviewService reviewService) {
        registerUserRoutes(app);
        registerCourseRoutes(app, reviewService);
        registerReviewRoutes(app, reviewService);
    }

    private static void registerUserRoutes(Javalin app) {
        UserService userService = new UserService();
        UserController userController = new UserController(userService);

        
        app.get("/users", userController::getAllUsers);
        app.get("/users/{id}", userController::getUserById);
        app.post("/users", userController::createUser);
        app.put("/users/{id}", userController::updateUser);
        app.delete("/users/{id}", userController::deleteUser);
    }

    private static void registerCourseRoutes(Javalin app, ReviewService reviewService) {
        CourseService courseService = new CourseService(new HttpClientApi());
        ResultsRepository resultsRepository = new ResultsRepository("data/historique_cours_prog_117510.csv");
        ResultsService resultsService = new ResultsService(resultsRepository);
        CourseController courseController = new CourseController(courseService, resultsService);


        app.get("/courses", courseController::getAllCourses);
        app.get("/courses/compare", courseController::compareCourses);
        app.get("/programs/search", courseController::searchPrograms);
        app.get("/courses/search", courseController::searchCoursesAdvanced);
        app.get("/programs/courses", courseController::getCoursesInProgram);
        app.get("/semesters/{code}/courses", courseController::getCoursesBySemester);
        app.get("/courses/{id}", courseController::getCourseById);
        app.get("/courses/{id}/schedule", courseController::getCourseSchedule);
        app.post("/courses/{id}/eligibility", courseController::checkEligibility);
        app.get("/courses/{id}/results", courseController::getAcademicResults);
    }

    private static void registerReviewRoutes(Javalin app, ReviewService reviewService) {
        ReviewController reviewController = new ReviewController(reviewService);


        app.get("/reviews/{courseId}", reviewController::getReviews);
        app.post("/reviews", reviewController::createReview);
    }
}
