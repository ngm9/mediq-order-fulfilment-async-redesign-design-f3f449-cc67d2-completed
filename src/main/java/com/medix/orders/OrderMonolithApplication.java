package com.medix.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Entry point for the pharmacy Order Monolith.
 *
 * <p>The monolith owns the whole fulfilment sequence: prescription storage, order
 * persistence, payment capture, insurer claim filing and courier booking. Every stage
 * runs inline within the customer checkout request against a single PostgreSQL instance.
 */
@SpringBootApplication
public class OrderMonolithApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderMonolithApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(30))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }
}
