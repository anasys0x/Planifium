package com.diro.ift2255.service;

import com.diro.ift2255.model.dto.ReviewExtraction;

public interface LlmReviewParser {
    ReviewExtraction extract(String text);
}
