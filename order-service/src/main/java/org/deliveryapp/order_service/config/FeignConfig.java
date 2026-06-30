package org.deliveryapp.order_service.config;

import feign.Logger;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @EnableFeignClients scans the client/ package and creates a runtime
 * implementation of every @FeignClient interface (RestaurantClient
 * in our case) — same idea as Spring Data JPA generating repository
 * implementations from interfaces at startup.
 */
@Configuration
@EnableFeignClients(basePackages = "org.deliveryapp.order_service.client")
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        // BASIC logs method, URL, status code and execution time —
        // useful while learning how Feign resolves and calls services.
        return Logger.Level.BASIC;
    }
}
