package org.deliveryapp.auth_service.controller;

import org.deliveryapp.auth_service.AuthServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

class YamlTest {

    @Test
    void shouldLoadApplicationYaml() {
        ConfigurableApplicationContext context =
                new SpringApplicationBuilder(AuthServiceApplication.class)
                        .web(WebApplicationType.NONE)
                        .run();

        System.out.println(
                context.getEnvironment().getProperty("spring.datasource.url"));

        context.close();
    }
}