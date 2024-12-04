package com.example.auctionservice.service;

import com.example.auctionservice.dto.BidEventDTO;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;

public interface SseService {
    Flux<BidEventDTO<?>> registerClient(Long auctionId, String clientId);
    <T> void notifyBidFailure(Long auctionId, String bidderId, T data);
    void notifyNewHighestBid(Long auctionId, String winningBidderId, BigDecimal newHighestBid);
    void notifyAuctionOver(Long auctionId, String winningBidder);
}
