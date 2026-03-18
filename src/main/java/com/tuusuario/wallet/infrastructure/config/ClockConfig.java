package com.tuusuario.wallet.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Configuración de infraestructura compartida.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock applicationClock() {
        return Clock.systemUTC();
    }
}

