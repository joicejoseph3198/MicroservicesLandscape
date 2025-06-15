package com.example.auction_consumer.messaging;


import com.example.UtilService.base.Event;
import com.example.auction_consumer.entity.Auction;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Slf4j
public class AuctionKTable {
    private final ObjectMapper objectMapper;

    @Autowired
    public AuctionKTable(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public Consumer<KStream<String, Event<String, Object>>> auctionKTableFunc() {
        return input -> {
            KTable<String, Auction> table = input
                    .peek((key, event) ->
                            log.debug("Processing event: {} type: {}", key, event != null ? event.getEventType() : "NULL"))
                    .mapValues(event -> {
                        if (event == null) {
                            log.info("Received null event");
                            return null;
                        }
                        switch (event.getEventType()) {
                            case CREATE, UPDATE:
                                Object data = event.getData();
                                Auction auction = objectMapper.convertValue(data, Auction.class);
                                log.info("Processing {} event for auction: {}", event.getEventType(), event.getKey());
                                return auction;
                            case DELETE:
                                log.info("Processing DELETE event for auction: {} - creating tombstone", event.getKey());
                                return null;
                            default:
                                log.warn("Unknown event type: {}", event.getEventType());
                                return null;
                        }
                    })
                    .toTable(
                            Materialized.<String, Auction, KeyValueStore<Bytes, byte[]>>as("auctions-store")
                                    .withKeySerde(Serdes.String())
                                    .withValueSerde(new JsonSerde<>(Auction.class))
                    );

            // Log KTable changes
            table.toStream().foreach((key, auction) -> {
                if (auction == null) {
                    log.info("Auction DELETED from KTable: {}", key);
                } else {
                    log.info("Auction UPSERTED in KTable: {} -> Product SKU Code: {}", key, auction.getProductSkuCode());
                }
            });
        };
    }
}
