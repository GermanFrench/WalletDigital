package com.tuusuario.wallet.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio que representa un refresh token para rotación de sesión.
 */
@Getter
@EqualsAndHashCode(of = "id")
public final class RefreshToken {

    private final UUID id;
    private final UUID authUserId;
    private final String tokenHash;
    private final Instant expiresAt;
    private final boolean revoked;
    private final Instant createdAt;

    private RefreshToken(UUID id, UUID authUserId, String tokenHash, Instant expiresAt,
                         boolean revoked, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "El id es obligatorio");
        this.authUserId = Objects.requireNonNull(authUserId, "El authUserId es obligatorio");
        this.tokenHash = Objects.requireNonNull(tokenHash, "El tokenHash es obligatorio");
        this.expiresAt = Objects.requireNonNull(expiresAt, "La expiración es obligatoria");
        this.revoked = revoked;
        this.createdAt = Objects.requireNonNull(createdAt, "La fecha de creación es obligatoria");
    }

    public static RefreshToken create(UUID id, UUID authUserId, String tokenHash,
                                      Instant expiresAt, Instant createdAt) {
        return new RefreshToken(id, authUserId, tokenHash, expiresAt, false, createdAt);
    }

    public static RefreshToken restore(UUID id, UUID authUserId, String tokenHash,
                                       Instant expiresAt, boolean revoked, Instant createdAt) {
        return new RefreshToken(id, authUserId, tokenHash, expiresAt, revoked, createdAt);
    }

    public RefreshToken revoke() {
        return new RefreshToken(this.id, this.authUserId, this.tokenHash, this.expiresAt, true, this.createdAt);
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(this.expiresAt);
    }

    public boolean isValid(Instant now) {
        return !this.revoked && !isExpired(now);
    }
}
