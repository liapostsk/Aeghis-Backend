package com.tfg.aegis.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:8081",
                        "http://localhost:19000",
                        "http://localhost:19006",
                        "exp://192.168.1.30:8081",
                        "exp://192.168.1.30:19000",
                        "exp://localhost:8081",
                        "http://192.168.1.30:8081",
                        "http://192.168.1.29:8080",
                        "http://192.168.1.29:8081",
                        "exp://192.168.1.29:8081",
                        "http://10.0.2.2:3000",
                        "http://10.0.2.2:8080"

                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true);
    }
}
