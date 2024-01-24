package com.example.productcompositeservice.service.impl;

import com.example.UtilService.dto.ResponseDTO;
import com.example.productcompositeservice.dto.ProductAggregate;
import com.example.productcompositeservice.dto.ProductDTO;
import com.example.productcompositeservice.dto.ReviewDTO;
import com.example.productcompositeservice.messaging.ProductCompositeProducer;
import com.example.productcompositeservice.service.ProductCompositeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private final ProductCompositeIntegration integration;
    private final ProductCompositeProducer productCompositeProducer;
    @Autowired
    public ProductCompositeServiceImpl(ProductCompositeIntegration integration,
                                       ProductCompositeProducer productCompositeProducer) {
        this.integration = integration;
        this.productCompositeProducer = productCompositeProducer;
    }
    @Override
    public ResponseDTO<ProductAggregate> getProductAggregate(String productId) {
        ResponseDTO<ProductAggregate> aggregateResponseDTO = new ResponseDTO<>(Boolean.FALSE, "Couldn't find associated objects", null);
        ResponseDTO<ProductDTO> resultantProduct = integration.getProduct(productId);
        ResponseDTO<List<ReviewDTO>> associatedReviews = integration.getReviews(productId);
        ProductAggregate productAggregate = null;
        if(Objects.nonNull(resultantProduct.getData()) && Objects.nonNull(associatedReviews.getData())){
            productAggregate = new ProductAggregate(resultantProduct.getData(),associatedReviews.getData());
            aggregateResponseDTO.setData(productAggregate);
            aggregateResponseDTO.setMessage("Request processed successfully.");
            aggregateResponseDTO.setStatus(Boolean.TRUE);
            return aggregateResponseDTO;
        }

        return aggregateResponseDTO;
    }

    @Override
    public ResponseDTO<String> deleteComposite(String productId){
        // Calls Producer which sends productId or appropriate Event to a Topic
        productCompositeProducer.produceDeleteEvent(productId);
        return new ResponseDTO<>(Boolean.TRUE,"Request processed successfully.", HttpStatus.ACCEPTED.toString());
    }
}
