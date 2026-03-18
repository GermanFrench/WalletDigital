package com.tuusuario.wallet.application.usecase;

import java.util.Objects;

/**
 * Datos necesarios para crear una nueva cuenta.
 */
public record CreateAccountCommand(String name, String email, String currency) {

    public CreateAccountCommand {
        name = Objects.requireNonNull(name, "El nombre es obligatorio").trim();
        email = Objects.requireNonNull(email, "El email es obligatorio").trim();
        currency = Objects.requireNonNull(currency, "La moneda es obligatoria").trim();
    }
}

