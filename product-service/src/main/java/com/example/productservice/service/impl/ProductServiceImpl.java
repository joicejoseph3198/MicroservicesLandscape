package com.example.productservice.service.impl;

import com.example.productservice.service.ProductService;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    @Override
    public String getProduct(int productId) {
//        if (productId < 1) throw new InvalidInputException("Invalid productId:" + productId);
//        if (productId == 13) throw new NotFoundException("No product found for productId: " + productId);
        return "This is the requested Product. ";
    }
}
