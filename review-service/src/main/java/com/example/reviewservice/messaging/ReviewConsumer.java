package com.example.reviewservice.messaging;

import com.example.UtilService.base.Event;
import com.example.reviewservice.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.function.Consumer;

@Configuration
@Slf4j
public class ReviewConsumer {
    private final ReviewService reviewService;
    @Autowired
    public ReviewConsumer(ReviewService reviewService){
        this.reviewService = reviewService;
    }
    @Bean
    public <T> Consumer<Event<String,T>> reviewMessageProcessor() {
        return event -> {
            try{
                log.info("Processing event.");
                switch (event.getEventType()){
                    case DELETE -> {
                        log.info("Processing event: {}", event.getEventType(), event.getEventCreatedAt());
                        String productId = event.getKey();
                        reviewService.deleteAssociatedReview(productId);
                        log.info("Message processing completed.");
                    }
                }
            }catch (ClassCastException e) {
                log.error("Event data casting error: {}", e.getMessage());
            } catch (Exception e) {
                log.error("Error processing event: {}", e.getMessage(), e);
            }
        };
    }
}
