package com.tuusuario.wallet.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de documentación OpenAPI/Swagger.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI walletOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("wallet-core API")
                        .description("API para gestionar cuentas y depósitos de una billetera digital")
                        .version("v1")
                        .contact(new Contact().name("Wallet Team").email("dev@wallet.local"))
                        .license(new License().name("Apache 2.0")));
    }
}

