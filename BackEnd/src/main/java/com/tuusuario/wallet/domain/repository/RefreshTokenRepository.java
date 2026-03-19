package com.tuusuario.wallet.domain.repository;

import com.tuusuario.wallet.domain.model.RefreshToken;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida del dominio para refresh tokens.
 */
public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void revokeAllByAuthUserId(UUID authUserId);
}
