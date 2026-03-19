package com.tuusuario.wallet.application.usecase;

import com.tuusuario.wallet.domain.model.Account;

public interface CreateAccountUseCase {

    Account createAccount(CreateAccountCommand command);
}

