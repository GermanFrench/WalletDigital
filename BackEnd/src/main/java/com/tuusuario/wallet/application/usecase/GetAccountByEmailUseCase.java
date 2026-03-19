package com.tuusuario.wallet.application.usecase;

import com.tuusuario.wallet.domain.model.Account;

import java.util.Optional;

public interface GetAccountByEmailUseCase {

    Optional<Account> findAccountByEmail(String email);
}
