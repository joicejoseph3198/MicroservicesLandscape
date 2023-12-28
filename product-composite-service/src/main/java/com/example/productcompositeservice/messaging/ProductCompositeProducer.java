package com.example.productcompositeservice.messaging;

import com.example.UtilService.base.Event;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
@Slf4j
public class ProductCompositeProducer {
    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeProducer.class);
    private final StreamBridge streamBridge;
    private static final String PRODUCT_DELETE_BINDING = "productDelete-out-0";
    private static final String REVIEW_DELETE_BINDING = "reviewDelete-out-0";
    @Autowired
    public ProductCompositeProducer(StreamBridge streamBridge){
        this.streamBridge = streamBridge;
    }

    /**
     * Stream Bridge Approach: (Alternate way of publishing events using Spring Cloud Stream)
     * StreamBridge helps us to send arbitrary events from outside any binding function,
     * which is useful when the events are generated in foreign systems, such as the invocation of a web endpoint.
    **/

    public void produceDeleteEvent(String productId){
        LOG.info("Producing delete event(s) ...");
        Event<String,Object> event = new Event<>(Event.Type.DELETE, productId, null, ZonedDateTime.now());
        Message<Event<String,Object>> message = generateMessage(event);
        streamBridge.send(PRODUCT_DELETE_BINDING,message);
        streamBridge.send(REVIEW_DELETE_BINDING,message);
        LOG.info("Event(s) propagated.");
    }

    private Message<Event<String,Object>> generateMessage(Event<String,Object> event){
        // Could use this separate function in case I need to generate a more complex message
        return MessageBuilder
                .withPayload(event)
                .build();
    }

}
