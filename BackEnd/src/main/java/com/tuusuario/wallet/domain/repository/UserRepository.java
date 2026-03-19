package com.tuusuario.wallet.domain.repository;

import com.tuusuario.wallet.domain.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida del dominio para persistencia de usuarios.
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);
}

