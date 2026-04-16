package com.diro.ift2255.controller;

import com.diro.ift2255.model.Review;
import com.diro.ift2255.service.ReviewService;
import io.javalin.http.Context;

public class ReviewController {

    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    // GET /reviews/:courseId
public void getReviews(Context ctx) {
    String courseId = ctx.pathParam("courseId").trim().toUpperCase();
    ctx.json(service.getReviews(courseId));
}

public void createReview(Context ctx) {
    Review review;
    try {
        review = ctx.bodyAsClass(Review.class);
    } catch (Exception e) {
        ctx.status(422).json("JSON invalide");
        return;
    }

    if (review.getCourseId() == null || review.getCourseId().isBlank()) {
        ctx.status(422).json("courseId requis");
        return;
    }

    // NORMALISATION POUR NE PAS GET LES DOUBLONS
    review.setCourseId(review.getCourseId().trim().toUpperCase());

    service.addReview(review);
    ctx.status(201).json(review);
}


}
