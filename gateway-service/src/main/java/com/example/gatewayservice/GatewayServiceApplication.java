package com.example.gatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayServiceApplication {

//	@Bean
//	@LoadBalanced
//	public WebClient.Builder loadBalancedWebClientBuilder() {
//		return WebClient.builder();
//	}

	public static void main(String[] args) {
		SpringApplication.run(GatewayServiceApplication.class, args);
	}

}
