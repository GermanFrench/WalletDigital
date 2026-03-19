package com.tuusuario.wallet.interfaces.rest.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request para refrescar el access token.
 */
public record RefreshTokenRequest(@NotBlank String refreshToken) {
}
