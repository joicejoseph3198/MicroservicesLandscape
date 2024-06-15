package com.example.gatewayservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(1)
public class GlobalLoggingFilter implements GlobalFilter {

    private static final Logger logger = LoggerFactory.getLogger(GlobalLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.info("Incoming request: {} {}", exchange.getRequest().getMethod(), exchange.getRequest().getURI());
        exchange.getRequest().getHeaders().forEach((name, values) -> values.forEach(value -> logger.info("{}: {}", name, value)));

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            logger.info("Outgoing response: {}", exchange.getResponse().getStatusCode());
            exchange.getResponse().getHeaders().forEach((name, values) -> values.forEach(value -> logger.info("{}: {}", name, value)));
        }));
    }
}
