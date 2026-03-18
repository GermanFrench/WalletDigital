package com.tuusuario.wallet.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio para autenticación y autorización.
 */
@Getter
@EqualsAndHashCode(of = "id")
public final class AuthUser {

    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final String role;
    private final boolean enabled;

    private AuthUser(UUID id, String email, String passwordHash, String role, boolean enabled) {
        this.id = Objects.requireNonNull(id, "El id del usuario autenticable es obligatorio");
        this.email = normalizeEmail(email);
        this.passwordHash = Objects.requireNonNull(passwordHash, "El password hash es obligatorio");
        this.role = normalizeRole(role);
        this.enabled = enabled;
    }

    public static AuthUser create(UUID id, String email, String passwordHash, String role, boolean enabled) {
        return new AuthUser(id, email, passwordHash, role, enabled);
    }

    private static String normalizeEmail(String value) {
        String normalized = Objects.requireNonNull(value, "El email es obligatorio").trim().toLowerCase();
        if (normalized.isBlank() || !normalized.contains("@")) {
            throw new IllegalArgumentException("El email del usuario autenticable es inválido");
        }
        return normalized;
    }

    private static String normalizeRole(String value) {
        String normalized = Objects.requireNonNull(value, "El rol es obligatorio").trim().toUpperCase();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("El rol no puede estar vacío");
        }
        return normalized;
    }
}

