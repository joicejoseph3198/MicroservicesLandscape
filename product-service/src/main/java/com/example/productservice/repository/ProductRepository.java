package com.example.productservice.repository;

import com.example.productservice.entity.Product;
import com.example.productservice.enums.Category;
import com.example.productservice.enums.Connectivity;
import com.example.productservice.enums.Switches;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    Optional<Product> findById(String productId);

    Optional<Product> findBySkuCode(String skuCode);

    Boolean existsByBrandAndModelAndConnectivityAndKeySwitchesAndCategory(String brand, String model,
                                                                          Connectivity connectivity,
                                                                          Switches keySwitches, Category category);
}
