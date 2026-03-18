package com.tuusuario.wallet.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio que representa al titular de una cuenta.
 */
@Getter
@EqualsAndHashCode(of = "id")
public final class User {

    private final UUID id;
    private final String name;
    private final String email;

    private User(UUID id, String name, String email) {
        this.id = Objects.requireNonNull(id, "El id del usuario es obligatorio");
        this.name = normalizeName(name);
        this.email = normalizeEmail(email);
    }

    public static User create(UUID id, String name, String email) {
        return new User(id, name, email);
    }

    private static String normalizeName(String value) {
        String normalized = Objects.requireNonNull(value, "El nombre es obligatorio").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("El nombre del usuario no puede estar vacío");
        }
        return normalized;
    }

    private static String normalizeEmail(String value) {
        String normalized = Objects.requireNonNull(value, "El email es obligatorio").trim().toLowerCase();
        if (normalized.isEmpty() || !normalized.contains("@")) {
            throw new IllegalArgumentException("El email del usuario es inválido");
        }
        return normalized;
    }
}

