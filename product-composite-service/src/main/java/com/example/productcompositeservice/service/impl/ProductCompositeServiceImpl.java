package com.example.productcompositeservice.service.impl;

import com.example.UtilService.dto.ResponseDTO;
import com.example.productcompositeservice.dto.*;
import com.example.productcompositeservice.feign.AuctionClient;
import com.example.productcompositeservice.feign.ProductClient;
import com.example.productcompositeservice.feign.ReviewClient;
import com.example.productcompositeservice.messaging.ProductCompositeProducer;
import com.example.productcompositeservice.service.ProductCompositeService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.concurrent.CompletedFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private final ProductClient productClient;
    private final ReviewClient reviewClient;
    private final AuctionClient auctionClient;
    private final ProductCompositeProducer productCompositeProducer;
    @Autowired
    public ProductCompositeServiceImpl(ProductClient productClient, ReviewClient reviewClient, AuctionClient auctionClient,
                                       ProductCompositeProducer productCompositeProducer) {
        this.productClient = productClient;
        this.reviewClient = reviewClient;
        this.auctionClient = auctionClient;
        this.productCompositeProducer = productCompositeProducer;
    }
    @Override
    public CompletableFuture<ResponseDTO<ProductAggregate>> getProductAggregate(String skuCode) {
        log.info("Fetching data for product (SKU: {})",skuCode.toUpperCase());
        ResponseDTO<ProductAggregate> responseDTO = new ResponseDTO<>(Boolean.FALSE, "Data not available", null);
        // async calls to fetch associated data
        CompletableFuture<ResponseDTO<ConfigureProductDTO>> fetchProductData = CompletableFuture.supplyAsync(()-> {
            return productClient.getProductBySkuCode(skuCode);
        });
        CompletableFuture<ResponseDTO<List<ReviewDTO>>> fetchReviewData = CompletableFuture.supplyAsync(()-> {
            return reviewClient.getReviewByProductId(skuCode);
        });
        CompletableFuture<ResponseDTO<AuctionDetailsDTO>> fetchAuctionData = CompletableFuture.supplyAsync(()-> {
            return auctionClient.findAuctionBySkuCode(skuCode);
        });

        return CompletableFuture.allOf(fetchProductData,fetchAuctionData,fetchReviewData).thenApply(placeholder -> {
            try{
                ResponseDTO<ConfigureProductDTO> productDetails = fetchProductData.get();
                ResponseDTO<List<ReviewDTO>> reviewDetails = fetchReviewData.get();
                ResponseDTO<AuctionDetailsDTO> auctionDetails = fetchAuctionData.get();
                if(productDetails.getStatus().equals(Boolean.TRUE)){
                    responseDTO.setStatus(Boolean.TRUE);
                    responseDTO.setMessage("Requested data fetched successfully.");
                    responseDTO.setData(new ProductAggregate(productDetails.getData(),reviewDetails.getData(), auctionDetails.getData()));
                }
            }catch (InterruptedException | ExecutionException e){
                log.error("Ran into an exception while fetching details for product (SKU: {})", skuCode.toUpperCase());
                log.error("Exception : {}", e.getMessage());
            }
            return responseDTO;
        });
    }

    @Override
    public ResponseDTO<String> deleteComposite(String productId){
        // Calls Producer which sends productId or appropriate Event to a Topic
        productCompositeProducer.produceDeleteEvent(productId);
        return new ResponseDTO<>(Boolean.TRUE,"Request processed successfully.", HttpStatus.ACCEPTED.toString());
    }

    @Override
    public ResponseDTO<String> createProduct(ConfigureProductDTO request) {
        productCompositeProducer.produceCreateProductEvent(request);
        return new ResponseDTO<>(Boolean.TRUE,"Request processed successfully.", HttpStatus.ACCEPTED.toString());
    }
}
