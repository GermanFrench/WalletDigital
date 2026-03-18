package com.tuusuario.wallet.application.usecase;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Datos necesarios para registrar un depósito.
 */
public record DepositMoneyCommand(BigDecimal amount) {

    public DepositMoneyCommand {
        amount = Objects.requireNonNull(amount, "El monto es obligatorio");
    }
}

