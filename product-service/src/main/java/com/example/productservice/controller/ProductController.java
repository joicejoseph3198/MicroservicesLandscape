package com.example.productservice.controller;

import com.example.UtilService.dto.ResponseDTO;
import com.example.productservice.dto.ConfigureProductDTO;
import com.example.productservice.dto.FilterProductDTO;
import com.example.productservice.enums.Status;
import com.example.productservice.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
@Slf4j
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @GetMapping(value = "/{id}")
    public ResponseDTO<ConfigureProductDTO> getProductBySkuCode(@PathVariable String id){
        return productService.getProductBySkuCode(id);
    }

    @DeleteMapping(value = "/delete/{productId}")
    public ResponseDTO<String> deleteProduct(@PathVariable String productId){
        return productService.deleteProduct(productId);
    }

    @PostMapping(value = "/fetchPage")
    Page<ConfigureProductDTO> fetchProductPage(@RequestBody FilterProductDTO filterProductDTO, HttpServletRequest request){
        return productService.filterData(filterProductDTO);
    }

    @PutMapping("/status")
    public ResponseDTO<String> updateProductStatus(
            @RequestParam String skuCode,
            @RequestParam Status productStatus){
        return productService.updateProductStatus(skuCode,productStatus);
    }
}
