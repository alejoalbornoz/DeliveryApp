package org.deliveryapp.payment_service.config;

import com.mercadopago.MercadoPagoConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MercadoPagoConfiguration {

    @Value("APP_USR-3921570013156020-071414-a8515b9ebe9c7588f182bbfcafa58c65-1883134549")
    private String accessToken;

    /**
     * Initializes the MercadoPago SDK with the access token on startup.
     * @PostConstruct runs after Spring injects all @Value fields,
     * so the token is guaranteed to be available here.
     */
    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
        log.info("MP token starts with: {}", accessToken.substring(0, 15));
    }
}