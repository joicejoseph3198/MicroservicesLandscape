package com.example.productservice.repository;

import com.example.productservice.entity.Product;
import com.example.productservice.enums.Category;
import com.example.productservice.enums.Connectivity;
import com.example.productservice.enums.Switches;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    Optional<Product> findBySkuCode(String skuCode);

    Boolean existsByBrandNameAndModelNumberAndConnectivityAndSwitchesAndCategory(String brandName, String modelNumber,
                                                                          Connectivity connectivity,
                                                                          Switches switches, Category category);
}
