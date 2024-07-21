package com.example.productservice.controller;

import com.example.UtilService.dto.ResponseDTO;
import com.example.productservice.dto.ConfigureProductDTO;
import com.example.productservice.dto.FilterProductDTO;
import com.example.productservice.dto.ProductDTO;
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
    public ResponseDTO<ConfigureProductDTO> getProductById(@PathVariable String id){
        return productService.getProductById(id);
    }

    @PostMapping(value = "/create")
    public ResponseDTO<String> createProduct(@RequestBody ConfigureProductDTO productDTO){
        return productService.createProduct(productDTO);
    }

    @DeleteMapping(value = "/delete/{productId}")
    public ResponseDTO<String> deleteProduct(@PathVariable String productId){
        return productService.deleteProduct(productId);
    }

//    @GetMapping(value = "/generateDummy")
//    ResponseDTO<String> generateDummyRecord(){
//        return productService.insertDummyData();
//    }

    @PostMapping(value = "/fetchPage")
    Page<ConfigureProductDTO> fetchProductPage(@RequestBody FilterProductDTO filterProductDTO, HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");
        log.debug("Received Authorization header: {}", authHeader);
        return productService.filterData(filterProductDTO);
    }
}
