package com.tuusuario.wallet.application.usecase;

/**
 * Caso de uso: solicitar un token de recuperación de contraseña.
 * Retorna el token (en producción se enviaría por email).
 */
public interface RequestPasswordResetUseCase {

    String requestPasswordReset(String email);
}
