package com.example.productcompositeservice.dto;


import com.example.productcompositeservice.enums.Rating;

public record ReviewDTO(String productId, Rating rating, String title, String description) {

}
