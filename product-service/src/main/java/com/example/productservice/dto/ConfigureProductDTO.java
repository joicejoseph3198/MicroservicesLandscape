package com.example.productservice.dto;

import com.example.productservice.enums.*;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ConfigureProductDTO(
        @NotNull String productName,
        @NotNull String productDescription,
        @NotNull String brandName,
        String modelNumber,
        Switches switches,
        KeyCaps keyCaps,
        Layout layout,
        Category category,
        @NotNull String skuCode,
        Float weight,
        SizeDTO size,
        @NotNull Float buyNowPrice,
        @NotNull Float bidStartPrice,
        String status,
        List<String> productImages) {
}
