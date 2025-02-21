package com.example.productservice.repository;

import com.example.productservice.entity.Product;
import com.example.productservice.enums.Status;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    Optional<Product> findBySkuCode(String skuCode);

    Boolean existsByModelNumber(String modelNumber);

    List<Product> findByStatusNotIn(List<Status> statusList);
}
