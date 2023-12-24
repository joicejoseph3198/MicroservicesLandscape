package com.example.reviewservice.service;

import com.example.UtilService.dto.ResponseDTO;
import com.example.reviewservice.dto.ReviewDTO;

import java.util.List;

public interface ReviewService {
    ResponseDTO<String> createReview(ReviewDTO reviewDTO);
    ResponseDTO<List<ReviewDTO>> getReviewByProductId(String productId);
    ResponseDTO<String> deleteReview(Integer reviewId);
    ResponseDTO<List<Integer>> deleteAssociatedReview(String productId);
    ResponseDTO<String> insertDummyData();
}
