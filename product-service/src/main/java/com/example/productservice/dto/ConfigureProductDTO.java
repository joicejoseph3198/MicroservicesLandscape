package com.example.productservice.dto;

import com.example.productservice.enums.*;

import java.util.List;

public record ConfigureProductDTO(
        String productName,
        String productDescription,
        String brandName,
        String modelNumber,
        Switches switches,
        KeyCaps keyCaps,
        Layout layout,
        Category category,
        String skuCode,
        Float weight,
        SizeDTO size,
        Float buyNowPrice,
        Float bidStartPrice,
        String status,
        List<String> productImages) {
}
