package com.example.productservice.controller;

import com.example.UtilService.dto.ResponseDTO;
import com.example.productservice.dto.ProductDTO;
import com.example.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService){
        this.productService = productService;
    }
    @GetMapping(value = "/{skuCode}")
    public ResponseDTO<ProductDTO> getProductBySkuCode(@PathVariable String skuCode){
        return productService.getProductBySkuCode(skuCode);
    }

    @PostMapping(value = "/create")
    public ResponseDTO<String> createProduct(@RequestBody ProductDTO productDTO){
        return productService.createProduct(productDTO);
    }

}
