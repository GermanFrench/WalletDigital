package com.tuusuario.wallet.application.usecase;

import java.util.Objects;

/**
 * Datos necesarios para autenticación.
 */
public record AuthenticateUserCommand(String email, String password) {

    public AuthenticateUserCommand {
        email = Objects.requireNonNull(email, "El email es obligatorio").trim().toLowerCase();
        password = Objects.requireNonNull(password, "La contraseña es obligatoria");
    }
}

