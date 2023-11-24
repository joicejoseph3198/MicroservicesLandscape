package com.example.productcompositeservice.controller;

import com.example.productcompositeservice.service.ProductCompositeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/productComposite")
public class ProductCompositeController {

    private final ProductCompositeService productCompositeService;

    @Autowired
    public ProductCompositeController(ProductCompositeService productCompositeService){
        this.productCompositeService = productCompositeService;
    }
    @GetMapping(value = "/{id}")
    public String getProductAggregate(@PathVariable int id){
        String result = productCompositeService.getProductAggregate(id);
        return result;
    }
}
