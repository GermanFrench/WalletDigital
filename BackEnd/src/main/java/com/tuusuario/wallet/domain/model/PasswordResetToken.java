package com.tuusuario.wallet.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio para tokens de recuperación de contraseña.
 */
@Getter
@EqualsAndHashCode(of = "id")
public final class PasswordResetToken {

    private final UUID id;
    private final String email;
    private final String tokenHash;
    private final Instant expiresAt;
    private final boolean used;
    private final Instant createdAt;

    private PasswordResetToken(UUID id, String email, String tokenHash, Instant expiresAt,
                               boolean used, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "El id es obligatorio");
        this.email = Objects.requireNonNull(email, "El email es obligatorio");
        this.tokenHash = Objects.requireNonNull(tokenHash, "El tokenHash es obligatorio");
        this.expiresAt = Objects.requireNonNull(expiresAt, "La expiración es obligatoria");
        this.used = used;
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de creación es obligatoria");
    }

    public static PasswordResetToken create(UUID id, String email, String tokenHash,
                                            Instant expiresAt, Instant createdAt) {
        return new PasswordResetToken(id, email, tokenHash, expiresAt, false, createdAt);
    }

    public static PasswordResetToken restore(UUID id, String email, String tokenHash,
                                             Instant expiresAt, boolean used, Instant createdAt) {
        return new PasswordResetToken(id, email, tokenHash, expiresAt, used, createdAt);
    }

    public PasswordResetToken markAsUsed() {
        return new PasswordResetToken(this.id, this.email, this.tokenHash, this.expiresAt, true, this.createdAt);
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(this.expiresAt);
    }

    public boolean isValid(Instant now) {
        return !this.used && !isExpired(now);
    }
}
