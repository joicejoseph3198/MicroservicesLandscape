package com.example.productcompositeservice.config;

import com.example.productcompositeservice.service.impl.ProductCompositeIntegration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class MicroServiceHealthCheckConfig implements HealthIndicator {
    private final ProductCompositeIntegration integration;
    @Autowired
    public MicroServiceHealthCheckConfig(ProductCompositeIntegration integration){
        this.integration = integration;
    }

    // Rewriting this in gateway service
    @Override
    public Health health() {
        final Map<String, String> registry = new LinkedHashMap<>();
        registry.put("review", integration.getReviewHealth());
        registry.put("product", integration.getProductHealth());

        if (registry.containsValue("DOWN")){
            return Health.down().withDetails(registry).build();
        }
        return Health.up().withDetails(registry).build();
    }
}
