package com.tuusuario.wallet.interfaces.rest.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request para solicitar recuperación de contraseña.
 */
public record ForgotPasswordRequest(@NotBlank @Email String email) {
}
