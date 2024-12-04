package com.example.auctionservice.controller;

import com.example.UtilService.dto.ResponseDTO;
import com.example.auctionservice.dto.AuctionDetailsDTO;
import com.example.auctionservice.dto.AuctionScheduleDTO;
import com.example.auctionservice.dto.BidEventDTO;
import com.example.auctionservice.enums.AuctionStatus;
import com.example.auctionservice.enums.BidEventType;
import com.example.auctionservice.service.AuctionService;
import com.example.auctionservice.service.SseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/auction")
@Slf4j
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
    public ResponseDTO<String> scheduleAuction(@RequestBody AuctionScheduleDTO request){
        return auctionService.scheduleAuction(request);
    }

    @DeleteMapping("/{skuCode}")
    @Operation(summary = "END a given AUCTION", security = @SecurityRequirement(name = "Bearer Authentication"))
    public ResponseDTO<String> endAuction(@PathVariable(value = "skuCode") String skuCode){
        return auctionService.endAuction(skuCode);
    }

    @Operation(summary = "find AUCTION associated with provided SKU_CODE")
    @GetMapping(value = "/{skuCode}")
    public ResponseDTO<AuctionDetailsDTO> findAuctionBySkuCode(@PathVariable String skuCode){
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
                         "Clients can register for SSE by calling this endpoint with their email id aka clientId.")
    @GetMapping(value = "/{auctionId}/bid-events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BidEventDTO<?>> streamBidEvents(
            @PathVariable Long auctionId,
            @RequestParam String clientId
    ) {
        return sseService.registerClient(auctionId, clientId)
                .onErrorResume(ex -> {
                    log.error("Error in bid events stream for auction {} and client {}",
                            auctionId, clientId, ex);
                    return Flux.empty();
                })
                .doFinally(signalType -> {
                    log.info("Bid events stream completed for auction {} and client {} with signal {}",
                            auctionId, clientId, signalType);
                })
                .timeout(Duration.ofMinutes(30),
                        Flux.just(new BidEventDTO<>(BidEventType.CONNECTION_DISRUPTED.name(), "Connection timed out", LocalDateTime.now())));
    }

}
