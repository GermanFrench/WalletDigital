package com.tuusuario.wallet.domain.repository;

import com.tuusuario.wallet.domain.model.Account;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida del dominio para cuentas.
 */
public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findById(UUID id);
}

