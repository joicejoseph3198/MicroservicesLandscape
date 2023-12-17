package com.example.reviewservice.controller;

import com.example.UtilService.dto.ResponseDTO;
import com.example.reviewservice.dto.ReviewDTO;
import com.example.reviewservice.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/review")
public class ReviewController {
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService){
        this.reviewService = reviewService;
    }

    @PostMapping(value = "/create")
    ResponseDTO<String> createReview(ReviewDTO reviewDTO){
        return reviewService.createReview(reviewDTO);
    }
    @GetMapping(value = "/associated/{id}")
    ResponseDTO<List<ReviewDTO>> getReviewByProductId(@PathVariable String id){
        return reviewService.getReviewByProductId(id);
    }
    @GetMapping(value = "/generateDummy")
    ResponseDTO<String> generateDummyRecord(){
        return reviewService.insertDummyData();
    }

}
