package com.tuusuario.wallet.application.usecase;

/**
 * Caso de uso: revocar todos los refresh tokens de un usuario (logout).
 */
public interface LogoutUseCase {

    void logout(String rawRefreshToken);
}
