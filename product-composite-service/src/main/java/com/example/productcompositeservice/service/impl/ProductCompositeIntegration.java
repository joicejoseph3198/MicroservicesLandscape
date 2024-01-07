package com.example.productcompositeservice.service.impl;

import com.example.UtilService.HttpErrorInfo;
import com.example.UtilService.exception.InvalidInputException;
import com.example.UtilService.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Objects;
@Component
public class ProductCompositeIntegration {
    @Bean
    RestTemplate restTemplate(){
        return new RestTemplate();
    }

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String productServiceUrl;
    private final String reviewServiceUrl;

    private final String reviewHealthUrl;
    private final String productHealthUrl;

    private static final String PRODUCT_SERVICE_URL = "http://product";
    private static final String REVIEW_SERVICE_URL = "http://review";

    @Autowired
    public ProductCompositeIntegration(
            @Lazy RestTemplate restTemplate,
            ObjectMapper mapper) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        productHealthUrl = PRODUCT_SERVICE_URL + "/actuator/health";
        reviewHealthUrl = REVIEW_SERVICE_URL + "/actuator/health";
        productServiceUrl = PRODUCT_SERVICE_URL;
        reviewServiceUrl = REVIEW_SERVICE_URL;
    }
    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ioex.getMessage();
        }
    }
    public String getProduct(int productId){
        try {
            String url = productServiceUrl +"/product/" + productId;
            LOG.debug("Will call getProduct API on URL: {}", url);
            String product = restTemplate.getForObject(url,String.class);
            LOG.debug("Found a product: {}", product);
            return product;
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
    public String getReviews(int productId) {
        try {
            String url = reviewServiceUrl + productId;

            LOG.debug("Will call getReviews API on URL: {}", url);
            String reviews = restTemplate.getForObject(url,String.class);

            LOG.debug("Found {} reviews for a product: {}", reviews, productId);
            return reviews;

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
