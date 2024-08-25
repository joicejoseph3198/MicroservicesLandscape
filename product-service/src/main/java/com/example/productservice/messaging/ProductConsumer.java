package com.example.productservice.messaging;

import com.example.UtilService.base.Event;
import com.example.UtilService.dto.ResponseDTO;
import com.example.productservice.dto.ConfigureProductDTO;
import com.example.productservice.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.function.Consumer;

@Configuration
@Slf4j
public class ProductConsumer {
    private final ProductService productService;
    private final ObjectMapper objectMapper;
    @Autowired
    public ProductConsumer(ProductService productService, ObjectMapper objectMapper){
        this.productService = productService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public <T> Consumer<Event<String,T>> productMessageProcessor() {
        return event -> {
            try{
                log.info("Processing event");
                switch (event.getEventType()){
                    case CREATE -> {
                        log.info("Processing event: {}, {}, {}", event.getEventType(), event.getKey(), event.getEventCreatedAt());
                        Object eventData = event.getData();
                        ConfigureProductDTO request = objectMapper.convertValue(eventData,ConfigureProductDTO.class);
                        ResponseDTO<String> response = productService.createProduct(request);
                        log.debug(response.getData());
                        log.info("Event processing completed.");
                    }
                    case DELETE -> {
                        log.info("Processing event: {}, {}, {}", event.getEventType(), event.getKey(), event.getEventCreatedAt());
                        String productId = event.getKey();
                        ResponseDTO<String> response = productService.deleteProduct(productId);
                        log.debug(response.getData());
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
