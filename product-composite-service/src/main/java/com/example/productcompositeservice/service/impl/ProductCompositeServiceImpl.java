package com.example.productcompositeservice.service.impl;

import com.example.productcompositeservice.service.ProductCompositeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private final ProductCompositeIntegration integration;
    @Autowired
    public ProductCompositeServiceImpl(ProductCompositeIntegration integration) {
        this.integration = integration;
    }

    @Override
    public String getProductAggregate(int productId) {
        String resultantProduct = integration.getProduct(productId);
        String associatedReview = integration.getReviews(productId);
        return resultantProduct + associatedReview;
    }
}
