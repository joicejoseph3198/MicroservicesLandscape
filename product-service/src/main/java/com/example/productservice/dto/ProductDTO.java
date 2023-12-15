package com.example.productservice.dto;

import com.example.productservice.enums.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDTO {
    private String skuCode;
    private String brand;
    private String model;
    private Connectivity connectivity;
    private Switches keySwitches;
    private KeyCaps keyCaps;
    private Layout layout;
    private Category category;
    private String description;
    private String dimension;
    private Float weight;
    private Float price;
    private String imageUrl;
}
