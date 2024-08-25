package com.example.auctionservice.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {
    SseEmitter registerClient(Long auctionId, String clientId);
    <T> void notifyClient(Long auctionId, String clientId, T data);
    <T> void notifyAllClients(Long auctionId, T data);
}
