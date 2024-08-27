package com.example.auctionservice.controller;

import com.example.UtilService.dto.ResponseDTO;
import com.example.auctionservice.dto.AuctionScheduleDTO;
import com.example.auctionservice.entity.Auction;
import com.example.auctionservice.enums.Status;
import com.example.auctionservice.service.AuctionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auction")
public class AuctionController {
    private final AuctionService auctionService;

    @Autowired
    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @PostMapping("/")
    @Operation(summary = "SCHEDULE an AUCTION by providing required information", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseDTO<String> scheduleAuction(AuctionScheduleDTO request){
        return auctionService.scheduleAuction(request);
    }

    @Operation(summary = "find AUCTION associated with provided SKU_CODE")
    @GetMapping(value = "/{skuCode}")
    public ResponseDTO<Auction> findAuctionBySkuCode(@PathVariable String skuCode){
        return auctionService.findAuctionBySkuCode(skuCode);
    }

    @Operation(summary = "update STATUS of an AUCTION", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PatchMapping("/")
    public ResponseDTO<String> updateAuctionStatus(
            @RequestParam(value = "auctionId") Long auctionId,
            @RequestParam(value = "status") Status status){
        return auctionService.updateStatus(auctionId,status);
    }

}
