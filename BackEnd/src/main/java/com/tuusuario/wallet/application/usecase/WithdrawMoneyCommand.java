package com.tuusuario.wallet.application.usecase;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Datos necesarios para un retiro.
 */
public record WithdrawMoneyCommand(BigDecimal amount) {

    public WithdrawMoneyCommand {
        amount = Objects.requireNonNull(amount, "El monto es obligatorio");
    }
}

