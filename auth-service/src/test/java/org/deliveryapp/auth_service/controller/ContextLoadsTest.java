package org.deliveryapp.auth_service.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ContextLoadsTest {

    @Value("${spring.datasource.url:NOT_FOUND}")
    String url;

    @Test
    void print() {
        System.out.println(url);
    }
}
