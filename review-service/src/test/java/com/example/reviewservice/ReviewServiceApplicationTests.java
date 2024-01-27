package com.example.reviewservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"eureka.client. enabled=false",
		"spring.cloud.config.enabled=false"})
class ReviewServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
