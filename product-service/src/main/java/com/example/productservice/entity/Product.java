package com.example.productservice.entity;

import com.example.productservice.enums.*;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@CompoundIndex(name = "switchCategoryBrand", def = "{'keySwitches': 1, 'category': 1,'brand': 1}")
public class Product{
    @Id
    private String id;
    @Indexed(name = "skuCodeIndex", unique = true)
    private String skuCode;
    private String brand;
    @Indexed(name = "modelNameIndex", unique = true)
    private String model;
    private Connectivity connectivity;
    private Switches keySwitches;
    private KeyCaps keyCaps;
    private Layout layout;
    private Category category;
    private String description;
    private String dimension;
    private Double weight;
    private Double price;
    private String imageUrl;
}
