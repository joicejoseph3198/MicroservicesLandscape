package com.example.auctionservice.messaging;

import com.example.UtilService.base.Event;
import com.example.auctionservice.dto.BidRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import java.time.ZonedDateTime;

@Slf4j
@Component
public class BidProducer {
    private final StreamBridge streamBridge;
    private static final String BID_BINDING = "bidPlace-out-0";

    @Autowired
    public BidProducer(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }


    private <K, T> Message<Event<K,T>> generateMessage(Event<K,T> event){
        return MessageBuilder
                .withPayload(event)
                .build();
    }

    public void placeBidEvent(BidRequestDTO bidRequestDTO){
        log.info("Producing event for bid processing ...");
        Event<Long, Object> event = new Event<>(Event.Type.CREATE, bidRequestDTO.auctionId(), bidRequestDTO, ZonedDateTime.now());
        Message<Event<Long, Object>> message = generateMessage(event);
        streamBridge.send(BID_BINDING, message);
        log.info("Event queued for further processing ...");
    }
}
