package com.example.productcompositeservice.service;

import com.example.UtilService.dto.ResponseDTO;

public interface ProductCompositeService {

    String getProductAggregate(int productId);
    ResponseDTO<String> deleteComposite(String productId);
}
