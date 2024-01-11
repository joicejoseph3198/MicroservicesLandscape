package com.example.gatewayservice.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class HealthCheckConfig implements HealthIndicator {

    private RestTemplate restTemplate = new RestTemplate();

    public String getHealth(String baseUrl) {
        String url = baseUrl + "/actuator/health";
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map<String, Object> respMap = response.getBody();
        return (String) respMap.get("status");
    }

    @Override
    public Health health() {
        final Map<String, String> registry = new LinkedHashMap<>();
        registry.put("review", getHealth("http://review"));
        registry.put("product", getHealth("http://product"));
        registry.put("composite", getHealth("http://composite"));

        if (registry.containsValue("DOWN")){
            return Health.down().withDetails(registry).build();
        }
        return Health.up().withDetails(registry).build();
    }
}
