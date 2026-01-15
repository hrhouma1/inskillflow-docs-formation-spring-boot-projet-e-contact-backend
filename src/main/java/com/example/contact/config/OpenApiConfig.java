package com.example.contact.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Serveur avec URL relative - fonctionne partout (localhost, Codespaces, prod)
        Server relativeServer = new Server()
                .url("/")
                .description("Current server (auto-detect)");

        return new OpenAPI()
                .servers(List.of(relativeServer))
                .info(new Info()
                        .title("Contact Form API")
                        .version("1.0")
                        .description("API REST pour formulaire de contact et gestion de leads")
                        .contact(new Contact()
                                .name("Support")
                                .email("support@example.com")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Entrez votre token JWT")));
    }
}

