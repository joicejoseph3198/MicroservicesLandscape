package com.example.productservice.service;

import com.example.UtilService.dto.ResponseDTO;
import com.example.productservice.dto.FilterProductDTO;
import com.example.productservice.dto.ProductDTO;
import org.springframework.data.domain.Page;

public interface ProductService {
    ResponseDTO<ProductDTO> getProductById(String skuCode);
    ResponseDTO<String> createProduct(ProductDTO productDTO);
    ResponseDTO<String> deleteProduct(String productId);
    ResponseDTO<String> insertDummyData();
    Page<ProductDTO> filterData(FilterProductDTO filterProductDTO);

}
