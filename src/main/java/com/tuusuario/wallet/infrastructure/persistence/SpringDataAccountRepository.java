package com.tuusuario.wallet.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataAccountRepository extends JpaRepository<AccountJpaEntity, UUID> {
}

