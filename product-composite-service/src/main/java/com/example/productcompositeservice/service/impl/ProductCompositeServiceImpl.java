package com.example.productcompositeservice.service.impl;

import com.example.UtilService.dto.ResponseDTO;
import com.example.productcompositeservice.service.ProductCompositeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private final ProductCompositeIntegration integration;
    @Autowired
    public ProductCompositeServiceImpl(ProductCompositeIntegration integration) {
        this.integration = integration;
    }

    /* TODO: Make this service non-blocking */
    @Override
    public String getProductAggregate(int productId) {
        String resultantProduct = integration.getProduct(productId);
        String associatedReview = integration.getReviews(productId);
        return resultantProduct + associatedReview;
    }

    @Override
    public ResponseDTO<String> deleteComposite(String productId){
        // Calls Producer which sends productId or appropriate Event to a Topic
        return new ResponseDTO<>(Boolean.TRUE,"Request processed successfully.", HttpStatus.ACCEPTED.toString());
    }
}
