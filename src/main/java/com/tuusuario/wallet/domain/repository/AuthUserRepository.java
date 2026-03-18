package com.tuusuario.wallet.domain.repository;

import com.tuusuario.wallet.domain.model.AuthUser;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida del dominio para autenticación.
 */
public interface AuthUserRepository {

    Optional<AuthUser> findByEmail(String email);

    Optional<AuthUser> findById(UUID id);

    AuthUser save(AuthUser authUser);
}

