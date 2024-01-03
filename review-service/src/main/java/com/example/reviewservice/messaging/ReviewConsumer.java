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
    private static final Logger LOG = LoggerFactory.getLogger(ReviewConsumer.class);
    private final ReviewService reviewService;
    @Autowired
    public ReviewConsumer(ReviewService reviewService){
        this.reviewService = reviewService;
    }
    @Bean
    public Consumer<Event<String,Object>> deleteMessageProcessor() {
        return event -> {
            LOG.info("Processing event: {}", event.toString());
            String productId = event.getKey();
            reviewService.deleteAssociatedReview(productId);
            LOG.info("Message processing completed.");
        };
    }
}
