package com.example.reviewservice.service.impl;

import com.example.reviewservice.service.ReviewService;
import org.springframework.stereotype.Service;

@Service
public class ReviewServiceImpl implements ReviewService {
    @Override
    public String getReviewByProductId(int productId) {
        return "And these are its associated Reviews.";
    }
}
