package com.example.productservice.service;

import com.example.UtilService.dto.ResponseDTO;
import com.example.productservice.dto.ConfigureProductDTO;
import com.example.productservice.dto.FilterProductDTO;
import com.example.productservice.dto.ProductDTO;
import org.springframework.data.domain.Page;

public interface ProductService {
    ResponseDTO<ConfigureProductDTO> getProductById(String skuCode);
    ResponseDTO<String> createProduct(ConfigureProductDTO productDTO);
    ResponseDTO<String> deleteProduct(String productId);
//    ResponseDTO<String> insertDummyData();
    Page<ConfigureProductDTO> filterData(FilterProductDTO filterProductDTO);

}
