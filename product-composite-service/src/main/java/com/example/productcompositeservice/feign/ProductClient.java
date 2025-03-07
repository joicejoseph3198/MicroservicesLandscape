package com.example.productcompositeservice.feign;

import com.example.UtilService.dto.ResponseDTO;
import com.example.productcompositeservice.dto.ConfigureProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("product")
public interface ProductClient {

    @GetMapping("/product/{id}")
    public ResponseDTO<ConfigureProductDTO> getProductBySkuCode(@PathVariable String id);
}
