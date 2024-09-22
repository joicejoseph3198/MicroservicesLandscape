package com.example.auctionservice.service.impl;

import com.example.auctionservice.enums.BidEventType;
import com.example.auctionservice.service.SseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SseServiceImpl implements SseService {
    // Map to store auction ID to client IDs and their emitters
    // Can be a redis HASH with TTL as auction expiry time
    private final Map<Long, Map<String, SseEmitter>> auctionEmitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter registerClient(Long auctionId, String clientId) {
        // Ensure the auction ID exists in the map
        auctionEmitters.computeIfAbsent(auctionId, k -> new ConcurrentHashMap<>());
        SseEmitter emitter = new SseEmitter(600000L); // 10 min
        emitter.onCompletion(() -> removeClient(auctionId, clientId)); // clientId here refers to user's email
        emitter.onTimeout(() -> removeClient(auctionId, clientId));
        auctionEmitters.get(auctionId).put(clientId, emitter);
        return emitter;
    }


    private void removeClient(Long auctionId, String clientId) {
        Map<String, SseEmitter> emitters = auctionEmitters.get(auctionId);
        if (emitters != null) {
            emitters.remove(clientId);
            if (emitters.isEmpty()) {
                auctionEmitters.remove(auctionId);
            }
        }
    }

    @Override
    public <T> void notifyBidFailure(Long auctionId, String bidderId, T message) {
        sendNotificationToClient(auctionId, bidderId, BidEventType.BID_FAILED, message);
    }

    @Override
    public void notifyNewHighestBid(Long auctionId, String winningBidderId, BigDecimal newHighestBid) {
        // Notify all participants
        sendNotificationToAll(auctionId, winningBidderId, BidEventType.NEW_HIGHEST_BID,
                "New highest bid: " + newHighestBid);
        // Notify the winning bidder
        sendNotificationToClient(auctionId, winningBidderId, BidEventType.BID_ACCEPTED,
                "Your bid of " + newHighestBid + " is now the highest bid.");

    }

    @Override
    public void notifyAuctionOver(Long auctionId, String winningBidderId) {
        // Notify all participants
        sendNotificationToAll(auctionId, winningBidderId, BidEventType.NEW_HIGHEST_BID,
                "Auction is over.");
        // Notify with the winner
        sendNotificationToClient(
                auctionId, winningBidderId,
                BidEventType.AUCTION_OVER,
                "Congratulations! Yours was the winning bid. An email will be shared with you shortly.");
    }

    private <T> void sendNotificationToClient(Long auctionId, String clientId, BidEventType eventType, T message) {
        Map<String, SseEmitter> auctionMap = auctionEmitters.get(auctionId);
        if (auctionMap != null) {
            SseEmitter emitter = auctionMap.get(clientId);
            if (emitter != null) {
                try {
                    emitter.send(SseEmitter.event()
                            .name(eventType.name())
                            .data(message));
                } catch (IOException e) {
                    removeClient(auctionId, clientId);
                }
            }
        }
    }

    private <T> void sendNotificationToAll(Long auctionId, String excludeClientId, BidEventType eventType, T message) {
        Map<String, SseEmitter> auctionMap = auctionEmitters.get(auctionId);
        if (auctionMap != null) {
            auctionMap.forEach((clientId, emitter) -> {
                if (!clientId.equals(excludeClientId)) {
                    try {
                        emitter.send(SseEmitter.event()
                                .name(eventType.name())
                                .data(message));
                    } catch (IOException e) {
                        removeClient(auctionId, clientId);
                    }
                }
            });
        }
    }

}
