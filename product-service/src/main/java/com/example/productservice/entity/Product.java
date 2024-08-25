package com.example.productservice.entity;

import com.example.productservice.enums.*;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product{
    @Id
    private String id;
    @Indexed(name = "skuCodeIndex", unique = true)
    private String skuCode;
    private String brandName;
    @Indexed(name = "modelNumberIndex", unique = true)
    private String modelNumber;
    private Connectivity connectivity;
    private Switches switches;
    private KeyCaps keyCaps;
    private Layout layout;
    private Category category;
    private String productDescription;
    private String productName;
    private Float weight;
    private Size size;
    private Float buyNowPrice;
    private Float bidStartPrice;
    private List<String> productImages;
    private Status status;
    private Status subStatus;
}
