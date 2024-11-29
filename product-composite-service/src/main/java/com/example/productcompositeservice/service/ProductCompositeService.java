package com.example.productcompositeservice.service;

import com.example.UtilService.dto.ResponseDTO;
import com.example.productcompositeservice.dto.ConfigureProductDTO;
import com.example.productcompositeservice.dto.ProductAggregate;

import java.util.concurrent.CompletableFuture;

public interface ProductCompositeService {

    CompletableFuture<ResponseDTO<ProductAggregate>> getProductAggregate(String skuCode);
    ResponseDTO<String> deleteComposite(String productId);
    ResponseDTO<String> createProduct(ConfigureProductDTO request);
}
