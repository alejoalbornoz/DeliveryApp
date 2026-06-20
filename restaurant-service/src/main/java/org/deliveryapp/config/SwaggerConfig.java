package org.deliveryapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI restaurantServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DeliveryApp - Restaurant Service API")
                        .description("Manages restaurants, categories and menu items")
                        .version("v1.0")
                        .contact(new Contact().name("DeliveryApp Team")));
    }
}
