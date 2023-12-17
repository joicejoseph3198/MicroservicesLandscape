package com.example.reviewservice.dto;

import com.example.reviewservice.enums.Rating;

public record ReviewDTO(String productId, Rating rating,String title,String description) {

}
