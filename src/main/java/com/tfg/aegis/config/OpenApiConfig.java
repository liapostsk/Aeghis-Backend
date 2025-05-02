package com.tfg.aegis.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import io.swagger.v3.parser.OpenAPIV3Parser;

@Configuration
public class OpenApiConfig {

    @Value("classpath:swagger/swagger-api-contract.yml")
    private Resource openApiResource;

    @Bean
    public OpenAPI customOpenAPI() throws IOException {
        try (InputStream is = openApiResource.getInputStream()) {
            String openApiContent = StreamUtils.copyToString(is, StandardCharsets.UTF_8);
            return new OpenAPIV3Parser().readContents(openApiContent).getOpenAPI();
        }
    }
}