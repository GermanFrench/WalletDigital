package com.tuusuario.wallet.application.usecase;

/**
 * Resultado de autenticación con token JWT.
 */
public record AuthenticationResult(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String refreshToken
) {
    public AuthenticationResult(String accessToken, String tokenType, long expiresInSeconds) {
        this(accessToken, tokenType, expiresInSeconds, null);
    }
}

