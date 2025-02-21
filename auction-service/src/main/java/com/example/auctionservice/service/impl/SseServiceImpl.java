package com.example.auctionservice.service.impl;

import com.example.auctionservice.dto.BidEventDTO;
import com.example.auctionservice.enums.BidEventType;
import com.example.auctionservice.service.SseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SseServiceImpl implements SseService {
    // Map to store auction ID to client IDs and their emitters
    // Sink for each auction to manage events
    private final Map<Long, Map<String, Sinks.Many<BidEventDTO<?>>>> auctionClientSinks =
            new ConcurrentHashMap<>();

    @Override
    public Flux<BidEventDTO<?>> registerClient(Long auctionId, String clientId) {
        // Create or get existing sink for this specific client in the auction
        Sinks.Many<BidEventDTO<?>> clientSink = auctionClientSinks
                .computeIfAbsent(auctionId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(clientId, k -> Sinks.many().multicast().onBackpressureBuffer());

        return clientSink.asFlux()
                .doOnSubscribe(subscription -> {
                    // Send initial connection event to this specific client
                    emitEventToClient(auctionId, clientId,
                            new BidEventDTO<>(BidEventType.CONNECTION_ESTABLISHED.name(),
                                    "Connected successfully for client: " + clientId,
                                    LocalDateTime.now())
                    );
                })
                .doOnCancel(() -> {
                    // Remove the sink when the client disconnects
                    Map<String, Sinks.Many<BidEventDTO<?>>> auctionClients =
                            auctionClientSinks.get(auctionId);
                    if (auctionClients != null) {
                        auctionClients.remove(clientId);
                        log.info("Removed sink for client {} in auction {}", clientId, auctionId);
                        // Clean up auction map if no more clients
                        if (auctionClients.isEmpty()) {
                            auctionClientSinks.remove(auctionId);
                            log.info("Cleaned up empty auction {}", auctionId);
                        }
                    }
                })
                .doOnError(error -> log.error("Stream error for auction {} and client {}: {}",
                        auctionId, clientId, error.getMessage(), error))
                .mergeWith(Flux.interval(Duration.ofMinutes(2))
                        .map(tick -> new BidEventDTO<>(BidEventType.HEART_BEAT.name(), "Ping", LocalDateTime.now())));
    }


    @Override
    public <T> void notifyBidFailure(Long auctionId, String bidderId, T message) {
        emitEventToClient(
                auctionId,
                bidderId,
                new BidEventDTO<>(
                BidEventType.BID_FAILED.name(),
                message,
                LocalDateTime.now()));
    }

    @Override
    public void notifyNewHighestBid(Long auctionId, String winningBidderId, BigDecimal newHighestBid) {
        // Broadcast to all participants
        emitEventToAllClients(auctionId,
                new BidEventDTO<>(
                        BidEventType.NEW_HIGHEST_BID.name(),
                        "New highest bid: " + newHighestBid,
                        LocalDateTime.now()));

        // Send specific message to winning bidder
        emitEventToClient(auctionId,
                winningBidderId,
                new BidEventDTO<>(
                        BidEventType.BID_ACCEPTED.name(),
                        "Your bid of " + newHighestBid + " is now the highest bid.",
                        LocalDateTime.now()));
    }

    @Override
    public void notifyAuctionOver(Long auctionId, String winningBidderId) {
        // Broadcast auction is concluded to all participants
        emitEventToAllClients(auctionId,
                new BidEventDTO<>(
                        BidEventType.AUCTION_OVER.name(),
                        "Auction is concluded.",
                        LocalDateTime.now()));

        // Send winner-specific notification
        emitEventToClient(auctionId,
                winningBidderId,
                new BidEventDTO<>(
                        BidEventType.AUCTION_OVER.name(),
                        "Congratulations! You have won the auction.",
                        LocalDateTime.now()));
    }

    public void emitEventToClient(Long auctionId, String clientId, BidEventDTO<?> event) {
        Map<String, Sinks.Many<BidEventDTO<?>>> auctionClients =
                auctionClientSinks.get(auctionId);
        if (auctionClients != null) {
            Sinks.Many<BidEventDTO<?>> clientSink = auctionClients.get(clientId);
            if (clientSink != null) {
                try {
                    clientSink.emitNext(event, Sinks.EmitFailureHandler.FAIL_FAST);
                } catch (Exception e) {
                    log.error("Failed to emit event for auction {} to client {}",
                            auctionId, clientId, e);
                }
            }
        }
    }

    public void emitEventToAllClients(Long auctionId, BidEventDTO<?> event) {
        Map<String, Sinks.Many<BidEventDTO<?>>> auctionClients =
                auctionClientSinks.get(auctionId);

        if (auctionClients != null) {
            auctionClients.values().forEach(clientSink -> {
                try {
                    clientSink.emitNext(event, Sinks.EmitFailureHandler.FAIL_FAST);
                } catch (Exception e) {
                    log.error("Failed to broadcast event for auction {}", auctionId, e);
                }
            });
        }
    }

}
