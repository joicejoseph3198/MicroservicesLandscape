package com.example.productservice.service;

import com.example.UtilService.dto.ResponseDTO;
import com.example.productservice.dto.ConfigureProductDTO;
import com.example.productservice.dto.FilterProductDTO;
import com.example.productservice.dto.ProductDTO;
import com.example.productservice.enums.Status;
import org.springframework.data.domain.Page;

public interface ProductService {
    ResponseDTO<ConfigureProductDTO> getProductBySkuCode(String skuCode);
    ResponseDTO<String> createProduct(ConfigureProductDTO productDTO);
    ResponseDTO<String> deleteProduct(String productId);
    Page<ConfigureProductDTO> filterData(FilterProductDTO filterProductDTO);
    ResponseDTO<String> updateProductStatus(String skuCode, Status status);
    ResponseDTO<String> syncSearch();

}
