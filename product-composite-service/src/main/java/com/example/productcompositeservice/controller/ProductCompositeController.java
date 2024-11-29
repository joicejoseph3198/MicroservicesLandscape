package com.example.productcompositeservice.controller;

import com.example.UtilService.dto.ResponseDTO;
import com.example.productcompositeservice.dto.ConfigureProductDTO;
import com.example.productcompositeservice.dto.ProductAggregate;
import com.example.productcompositeservice.service.ProductCompositeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(value = "/productComposite")
public class ProductCompositeController {

    private final ProductCompositeService productCompositeService;

    @Autowired
    public ProductCompositeController(ProductCompositeService productCompositeService){
        this.productCompositeService = productCompositeService;
    }

    @GetMapping(value = "/{id}")
    public CompletableFuture<ResponseDTO<ProductAggregate>> getProductAggregate(@PathVariable String id){
        return productCompositeService.getProductAggregate(id);
    }

    @DeleteMapping(value = "/delete/{productId}")
    public ResponseDTO<String> deleteProductComposite(@PathVariable String productId){
       return productCompositeService.deleteComposite(productId);
    }

    @PostMapping(value = "/configure/product")
    public ResponseDTO<String> createProduct(@RequestBody ConfigureProductDTO productDTO){
        return productCompositeService.createProduct(productDTO);
    }
}
