package com.tuusuario.wallet.interfaces.rest.response;

import com.tuusuario.wallet.domain.model.Account;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de salida para exponer información de una cuenta.
 */
public record AccountResponse(
        UUID id,
        UUID userId,
        BigDecimal balance,
        String currency,
        String status
) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getUserId(),
                account.getBalance(),
                account.getCurrency(),
                account.getStatus().name()
        );
    }
}

