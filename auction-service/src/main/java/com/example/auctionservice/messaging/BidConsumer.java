package com.example.auctionservice.messaging;

import com.example.UtilService.base.Event;
import com.example.UtilService.dto.ResponseDTO;
import com.example.auctionservice.dto.BidRequestDTO;
import com.example.auctionservice.service.BidService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZonedDateTime;
import java.util.function.Consumer;

@Slf4j
@Configuration
public class BidConsumer {
    private final BidService bidService;
    private final ObjectMapper objectMapper;

    @Autowired
    public BidConsumer(BidService bidService, ObjectMapper objectMapper) {
        this.bidService = bidService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public <K,T> Consumer<Event<K,T>> bidProcessor() {
        return event -> {
            try {
                switch (event.getEventType()) {
                    case CREATE -> {
                        log.info("Processing event: {}, {}, {}", event.getEventType(), event.getKey(), event.getEventCreatedAt());
                        Object eventData = event.getData();
                        BidRequestDTO request = objectMapper.convertValue(eventData, BidRequestDTO.class);
                        bidService.processBid(request);
                        log.info("Event processing completed.");
                    }
                    default -> log.warn("Unhandled event type: {}", event.getEventType());
                }
            }catch (ClassCastException e) {
                log.error("Event data casting error: {}", e.getMessage());
            } catch (Exception e) {
                log.error("Error processing event: {}", e.getMessage(), e);
            }
        };
    }
}
