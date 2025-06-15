package com.example.auctionservice.messaging;

import com.example.UtilService.base.Event;
import com.example.auctionservice.dto.BidRequestDTO;
import com.example.auctionservice.entity.Auction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.converter.KafkaMessageHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import java.time.ZonedDateTime;

@Slf4j
@Component
public class AuctionProducer {
    private final StreamBridge streamBridge;
    private static final String AUCTION_TABLE_BINDING = "auctionTable-out-0";

    @Autowired
    public AuctionProducer(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }


    private Message<Event<Long,Object>> generateMessage(Event<Long,Object> event){
        return MessageBuilder
                .withPayload(event)
                .build();
    }

    public void produceAuctionPublishEvent(Auction auction){
        log.info("Producing event for auction | Auction ID: {}", auction.getId());
        // Change to String key to match consumer
        Event<Long, Object> event = new Event<>(Event.Type.CREATE,
                auction.getId(), auction, ZonedDateTime.now());
        Message<Event<Long, Object>> message = generateMessage(event);
        streamBridge.send(AUCTION_TABLE_BINDING,message);
        log.info("Event queued for further processing | Auction ID: {}", auction.getId());
    }

    public void produceAuctionUnPublishEvent(Long auctionId){
        log.info("Producing event for auction | Auction ID: {}", auctionId);
        // Change to String key and keep DELETE event
        Event<Long, Object> event = new Event<>(Event.Type.DELETE,
                auctionId, null, ZonedDateTime.now());
        Message<Event<Long, Object>> message = generateMessage(event);
        streamBridge.send(AUCTION_TABLE_BINDING,message);
        log.info("Event queued for further processing | Auction ID: {}", auctionId);
    }

}
