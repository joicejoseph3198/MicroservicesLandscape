package com.example.auctionservice.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;

public interface SseService {
    SseEmitter registerClient(Long auctionId, String clientId);
    <T> void notifyBidFailure(Long auctionId, String bidderId, T data);
    void notifyNewHighestBid(Long auctionId, String winningBidderId, BigDecimal newHighestBid);
}
