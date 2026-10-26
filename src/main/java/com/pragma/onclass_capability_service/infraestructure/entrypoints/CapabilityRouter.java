package com.pragma.onclass_capability_service.infraestructure.entrypoints;

import com.pragma.onclass_capability_service.infraestructure.entrypoints.handler.CapabilityHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class CapabilityRouter {

    @Bean
    public RouterFunction<ServerResponse> technologyRoutes(CapabilityHandler handler) {
        return route()
                .POST("/api/v1/capabilities", handler::createTechnology)
                .GET("/api/v1/capabilities", handler::listCapabilities)
                .POST("/api/v1/capabilities/validate", handler::validateCapabilities)
                .build();
    }
}
