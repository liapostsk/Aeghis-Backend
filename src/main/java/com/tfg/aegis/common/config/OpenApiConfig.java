package com.tfg.aegis.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("API AEGHIS")
                .description("API para gestión de usuarios y recursos del sistema AEGHIS")
                .version("1.0.0")
            )
            .addSecurityItem(new SecurityRequirement().addList("JWT"))
            .components(new Components().addSecuritySchemes("JWT",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Introduce el token JWT para autenticación")
        ));
    }
}