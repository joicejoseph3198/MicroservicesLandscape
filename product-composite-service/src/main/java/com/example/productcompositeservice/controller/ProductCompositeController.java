package com.example.productcompositeservice.controller;

import com.example.UtilService.dto.ResponseDTO;
import com.example.productcompositeservice.messaging.ProductCompositeProducer;
import com.example.productcompositeservice.service.ProductCompositeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/productComposite")
public class ProductCompositeController {

    private final ProductCompositeService productCompositeService;
    private final ProductCompositeProducer productCompositeProducer;

    @Autowired
    public ProductCompositeController(ProductCompositeService productCompositeService,ProductCompositeProducer productCompositeProducer){
        this.productCompositeService = productCompositeService;
        this.productCompositeProducer = productCompositeProducer;
    }
    @GetMapping(value = "/{id}")
    public String getProductAggregate(@PathVariable int id){
        return productCompositeService.getProductAggregate(id);
    }

    @DeleteMapping(value = "/delete/{productId}")
    public ResponseDTO<HttpStatus> deleteProductComposite(@PathVariable String productId){
        productCompositeProducer.produceDeleteEvent(productId);
        return new ResponseDTO<>(Boolean.TRUE,"Request processed successfully.", HttpStatus.ACCEPTED);
    }
}
