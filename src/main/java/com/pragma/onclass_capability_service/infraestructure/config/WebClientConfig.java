package com.pragma.onclass_capability_service.infraestructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient technologyWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8081") // URL de tu onclass-technology-service
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
