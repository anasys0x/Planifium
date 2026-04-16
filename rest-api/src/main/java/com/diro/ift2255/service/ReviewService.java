package com.diro.ift2255.service;

import java.util.List;

import com.diro.ift2255.model.Review;
import com.diro.ift2255.repository.ReviewRepository;

public class ReviewService {

    private final ReviewRepository repo = new ReviewRepository();

    public void addReview(Review review) {
        repo.addReview(review);
    }

    public List<Review> getReviews(String courseId) {
        return repo.getReviews(courseId);
    }
}
