package com.example.productcompositeservice.feign;

import com.example.UtilService.dto.ResponseDTO;
import com.example.productcompositeservice.dto.ReviewDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("review")
public interface ReviewClient {
    @GetMapping(value = "/review/associated/{id}")
    ResponseDTO<List<ReviewDTO>> getReviewByProductId(@PathVariable String id);
}
