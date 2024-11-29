package com.example.productcompositeservice.feign;

import com.example.UtilService.dto.ResponseDTO;
import com.example.productcompositeservice.dto.AuctionDetailsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("auction")
public interface AuctionClient {

    @GetMapping(value = "/auction/{skuCode}")
    public ResponseDTO<AuctionDetailsDTO> findAuctionBySkuCode(@PathVariable String skuCode);
}
