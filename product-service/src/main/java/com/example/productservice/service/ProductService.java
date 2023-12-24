package com.example.productservice.service;

import com.example.UtilService.dto.ResponseDTO;
import com.example.productservice.dto.ProductDTO;

public interface ProductService {
    ResponseDTO<ProductDTO> getProductBySkuCode(String skuCode);
    ResponseDTO<String> createProduct(ProductDTO productDTO);
    ResponseDTO<String> deleteProduct(String productId);
    ResponseDTO<String> insertDummyData();

}
