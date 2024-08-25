package com.example.productcompositeservice.service;

import com.example.UtilService.dto.ResponseDTO;
import com.example.productcompositeservice.dto.ConfigureProductDTO;
import com.example.productcompositeservice.dto.ProductAggregate;

public interface ProductCompositeService {

    ResponseDTO<ProductAggregate> getProductAggregate(String productId);
    ResponseDTO<String> deleteComposite(String productId);
    ResponseDTO<String> createProduct(ConfigureProductDTO request);
}
