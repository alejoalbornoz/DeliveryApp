package org.deliveryapp.payment_service.config;

import com.mercadopago.MercadoPagoConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MercadoPagoConfiguration {

    @Value("${MERCADOPAGO_ACCESS_TOKEN}")
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
