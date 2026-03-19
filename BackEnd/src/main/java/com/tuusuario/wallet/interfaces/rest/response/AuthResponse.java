package com.tuusuario.wallet.interfaces.rest.response;

import com.tuusuario.wallet.application.usecase.AuthenticationResult;

/**
 * DTO de salida para autenticación.
 */
public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds, String refreshToken) {

    public static AuthResponse from(AuthenticationResult result) {
        return new AuthResponse(
                result.accessToken(),
                result.tokenType(),
                result.expiresInSeconds(),
                result.refreshToken()
        );
    }
}

