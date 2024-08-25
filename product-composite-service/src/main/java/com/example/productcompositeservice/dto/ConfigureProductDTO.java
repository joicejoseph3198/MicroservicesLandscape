package com.example.productcompositeservice.dto;

import com.example.productcompositeservice.enums.*;

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
