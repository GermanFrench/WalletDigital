package com.tuusuario.wallet.application.usecase;

import java.util.Objects;

/**
 * Comando de entrada para registrar un usuario autenticable.
 */
public record RegisterUserCommand(String email, String password) {

    public RegisterUserCommand {
        email = Objects.requireNonNull(email, "El email es obligatorio").trim().toLowerCase();
        password = Objects.requireNonNull(password, "La contraseña es obligatoria").trim();

        if (email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("El email debe ser válido");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
    }
}
