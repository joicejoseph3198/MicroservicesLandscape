package com.example.productcompositeservice.service.impl;

import com.example.UtilService.HttpErrorInfo;
import com.example.UtilService.dto.ResponseDTO;
import com.example.UtilService.exception.InvalidInputException;
import com.example.UtilService.exception.NotFoundException;
import com.example.productcompositeservice.dto.ProductDTO;
import com.example.productcompositeservice.dto.ReviewDTO;
import com.example.productcompositeservice.feign.ProductClient;
import com.example.productcompositeservice.feign.ReviewClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class ProductCompositeIntegration {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final RestTemplate restTemplate;
    private final ProductClient productClient;
    private final ReviewClient reviewClient;
    private final ObjectMapper mapper;
    private final String reviewHealthUrl;
    private final String productHealthUrl;
    private static final String PRODUCT_SERVICE_URL = "http://product";
    private static final String REVIEW_SERVICE_URL = "http://review";

    @Autowired
    public ProductCompositeIntegration(ObjectMapper mapper,
                                       ReviewClient reviewClient,
                                       ProductClient productClient,
                                       RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.reviewClient = reviewClient;
        this.productClient = productClient;
        this.mapper = mapper;
        productHealthUrl = PRODUCT_SERVICE_URL + "/actuator/health";
        reviewHealthUrl = REVIEW_SERVICE_URL + "/actuator/health";
    }
    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ioex.getMessage();
        }
    }
    public ResponseDTO<ProductDTO> getProduct(String productId){
        try {
            LOG.debug("Feign call: getProductById({})",productId);
            ResponseDTO<ProductDTO> response = productClient.getProductById(productId);
            LOG.debug("Found associated product for Id: ({})", productId);
            return response;
        } catch (HttpClientErrorException ex) {
            switch (Objects.requireNonNull(HttpStatus.resolve(ex.getStatusCode().value()))) {
                case NOT_FOUND -> throw new NotFoundException(getErrorMessage(ex));
                case UNPROCESSABLE_ENTITY -> throw new InvalidInputException(getErrorMessage(ex));
                default -> {
                    LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
                    LOG.warn("Error body: {}", ex.getResponseBodyAsString());
                    throw ex;
                }
            }
        }
    }
    public ResponseDTO<List<ReviewDTO>> getReviews(String productId) {
        try {
            LOG.debug("Feign Call: getReviewByProductId({})",productId);
            ResponseDTO<List<ReviewDTO>> response = reviewClient.getReviewByProductId(productId);
            LOG.debug("Found reviews for product Id: ({})", productId);
            return response;
        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting reviews, return zero reviews: {}", ex.getMessage());
            throw ex;
        }
    }

    public String getReviewHealth(){
        ResponseEntity<Map> response = restTemplate.getForEntity(reviewHealthUrl, Map.class);
        Map<String, Object> respMap = response.getBody();
        return (String) respMap.get("status");
    }
    public String getProductHealth(){
        ResponseEntity<Map> response = restTemplate.getForEntity(productHealthUrl, Map.class);
        Map<String, Object> respMap = response.getBody();
        return (String) respMap.get("status");
    }
}
