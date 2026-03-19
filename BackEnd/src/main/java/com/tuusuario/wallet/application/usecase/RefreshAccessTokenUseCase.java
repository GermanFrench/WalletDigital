package com.tuusuario.wallet.application.usecase;

/**
 * Caso de uso: refrescar access token usando un refresh token.
 */
public interface RefreshAccessTokenUseCase {

    AuthenticationResult refresh(String rawRefreshToken);
}
