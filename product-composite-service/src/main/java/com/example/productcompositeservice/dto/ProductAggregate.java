package com.example.productcompositeservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
public record ProductAggregate(ProductDTO productDTO, List<ReviewDTO> reviewDTOS) {
}
