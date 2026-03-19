package com.tuusuario.wallet.application.usecase;

import com.tuusuario.wallet.domain.model.Account;

import java.util.UUID;

public interface GetAccountUseCase {

    Account getAccount(UUID accountId);
}

