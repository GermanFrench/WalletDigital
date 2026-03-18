package com.tuusuario.wallet.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataAuthUserRepository extends JpaRepository<AuthUserJpaEntity, UUID> {

    Optional<AuthUserJpaEntity> findByEmail(String email);
}

