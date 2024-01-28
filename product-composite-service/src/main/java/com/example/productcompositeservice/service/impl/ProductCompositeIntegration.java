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
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Retry(name = "product")
//    @TimeLimiter(name = "product")
    @CircuitBreaker(name ="product", fallbackMethod = "getProductFallBackValue")
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

    /*
    * method must follow the signature of the method the circuit breaker is applied for and also
    * have an extra last argument used for passing the exception that triggered the circuit breaker.
    *
    * If a significant percentage of the calls are failing, the circuit breaker will switch to the OPEN state.
    * There is a wait duration that you can configure, then it will switch to HALF_OPEN state.
    * Circuit breaker will decide whether the circuit will be opened or closed based on the configurable number of calls
    * after the circuit has transitioned to the half-open state.You can configure what percentage of requests you want
    * to send successfully to the dependant service to move to the CLOSED state again. If it fails, it will move back
    * to the OPEN state again.
     */
    private ResponseDTO<ProductDTO> getProductFallBackValue(String productId, CallNotPermittedException ex){
        LOG.info("[FALLBACK METHOD]");
        // The fallback logic can look up information from alternative sources, for example, an internal cache
        if(productId.equals("101")){
            throw new NotFoundException("Product (Id: 101) couldn't be found in the internal cache.");
        }
        return new ResponseDTO<ProductDTO>(Boolean.TRUE,
                "We seem to have encountered some issue. Please try again after sometime",
                null);
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
