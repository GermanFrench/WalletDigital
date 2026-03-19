package com.tuusuario.wallet.application.usecase;

/**
 * Caso de uso: restablecer contraseña con token de recuperación.
 */
public interface ResetPasswordUseCase {

    void resetPassword(String rawToken, String newPassword);
}
