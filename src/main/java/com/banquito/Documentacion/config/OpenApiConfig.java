package com.banquito.Documentacion.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Documentación - Banco Banquito")
                        .description("Microservicio para la gestión de documentos adjuntos de solicitudes de crédito")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo Banquito")
                                .email("desarrollo@banquito.com")
                                .url("https://www.banquito.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8084")
                                .description("Servidor de Desarrollo"),
                        new Server()
                                .url("https://api-documentacion.banquito.com")
                                .description("Servidor de Producción")
                ));
    }
} 