package com.example.productservice.messaging;

import com.example.UtilService.base.Event;
import com.example.productservice.dto.SearchIndexDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.time.ZonedDateTime;

@Slf4j
@Configuration
public class SearchProducer {
    private final StreamBridge streamBridge;
    private static final String SEARCH_BINDING = "syncSearchIndex-out-0";

    @Autowired
    public SearchProducer(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }


    private <K, T> Message<Event<K,T>> generateMessage(Event<K,T> event){
        return MessageBuilder
                .withPayload(event)
                .build();
    }

    public void createSearchEntry(SearchIndexDTO searchIndexDTO){
        log.info("Producing event for search indexing");
        Event<String, Object> event = new Event<>(Event.Type.CREATE, searchIndexDTO.skuCode(), searchIndexDTO, ZonedDateTime.now());
        Message<Event<String, Object>> message = generateMessage(event);
        streamBridge.send(SEARCH_BINDING, message);
        log.info("Event queued for further processing");
    }

    public void deleteSearchEntry(String skuCode){
        log.info("Unpublishing product skuCode: {} from search indexing", skuCode);
        Event<String, Object> event = new Event<>(Event.Type.DELETE, skuCode, null, ZonedDateTime.now());
        Message<Event<String, Object>> message = generateMessage(event);
        streamBridge.send(SEARCH_BINDING, message);
        log.info("Queued for further processing");
    }
}
