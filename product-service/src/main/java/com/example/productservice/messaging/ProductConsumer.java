package com.example.productservice.messaging;

import com.example.UtilService.base.Event;
import com.example.UtilService.dto.ResponseDTO;
import com.example.productservice.service.ProductService;
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
    private static final Logger LOG = LoggerFactory.getLogger(ProductConsumer.class);
    private final ProductService productService;
    @Autowired
    public ProductConsumer(ProductService productService){
        this.productService = productService;
    }
    @Bean
    public Consumer<Event<String,Object>> deleteMessageProcessor() {
        return event -> {
            LOG.info("Processing event: {}", event);
            String productId = event.getKey();
            ResponseDTO<String> response = productService.deleteProduct(productId);
            LOG.debug(response.getData());
            LOG.info("Message processing completed.");
        };
    }
}
