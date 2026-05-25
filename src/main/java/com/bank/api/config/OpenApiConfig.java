package com.bank.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Bean
    public OpenAPI bankingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banking API — Sofka")
                        .description("API REST del sistema bancario: clientes, cuentas, movimientos y reportes.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("bank-sofka")
                                .url("https://github.com/jalvarezmen/bank-sofka")))
                .addServersItem(new Server()
                        .url("http://localhost:8080" + contextPath)
                        .description("Servidor local"));
    }
}
