package com.tuusuario.wallet.domain.repository;

import com.tuusuario.wallet.domain.model.PasswordResetToken;

import java.util.Optional;

/**
 * Puerto de salida del dominio para tokens de recuperación de contraseña.
 */
public interface PasswordResetTokenRepository {

    PasswordResetToken save(PasswordResetToken token);

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
}
