package com.example.productservice.dto;

import com.example.productservice.enums.*;

import java.util.List;

public record ConfigureProductDTO(
        String productName,
        String productDescription,
        String brandName,
        String modelNumber,
        Connectivity connectivity,
        Switches switches,
        KeyCaps keyCaps,
        Layout layout,
        Category category,
        String skuCode,
        Float weight,
        SizeDTO size,
        Float buyNowPrice,
        Float bidStartPrice,
        List<String> productImages) {
}
