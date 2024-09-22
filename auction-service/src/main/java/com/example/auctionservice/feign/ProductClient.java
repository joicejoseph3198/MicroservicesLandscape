package com.example.auctionservice.feign;

import com.example.UtilService.dto.ResponseDTO;
import com.example.auctionservice.enums.Status;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("product")
public interface ProductClient {

    @PutMapping("/product/status")
    public ResponseDTO<String> updateProductStatus(
            @RequestParam String skuCode,
            @RequestParam Status productStatus);

}
