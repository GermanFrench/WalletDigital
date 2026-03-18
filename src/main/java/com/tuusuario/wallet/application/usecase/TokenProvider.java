package com.tuusuario.wallet.application.usecase;

import com.tuusuario.wallet.domain.model.AuthUser;

public interface TokenProvider {

    String generateToken(AuthUser authUser);

    boolean isTokenValid(String token);

    String extractSubject(String token);

    long getExpirationInSeconds();
}

