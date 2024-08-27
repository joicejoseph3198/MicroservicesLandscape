package com.example.auctionservice.controller;

import com.example.UtilService.dto.ResponseDTO;
import com.example.auctionservice.dto.AuctionScheduleDTO;
import com.example.auctionservice.entity.Auction;
import com.example.auctionservice.enums.AuctionStatus;
import com.example.auctionservice.service.AuctionService;
import com.example.auctionservice.service.SseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/auction")
public class AuctionController {
    private final AuctionService auctionService;
    private final SseService sseService;

    @Autowired
    public AuctionController(AuctionService auctionService, SseService sseService) {
        this.auctionService = auctionService;
        this.sseService = sseService;
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
            @RequestParam(value = "status") AuctionStatus auctionStatus){
        return auctionService.updateStatus(auctionId, auctionStatus);
    }

    @Operation(summary = "API through which SERVER SENT EVENTS (SSE) are sent to the clients. " +
                         "Clients can register for SSE by calling this endpoint with their emaigil id aka clientId.")
    @GetMapping("/{auctionId}/bid-events")
    public SseEmitter streamBidEvents(@PathVariable Long auctionId, @RequestParam String clientId) {
        return sseService.registerClient(auctionId, clientId);
    }

}
