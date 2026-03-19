package com.tuusuario.wallet.application.usecase;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Datos necesarios para una transferencia entre cuentas.
 */
public record TransferMoneyCommand(UUID destinationAccountId, BigDecimal amount) {

    public TransferMoneyCommand {
        destinationAccountId = Objects.requireNonNull(destinationAccountId, "La cuenta destino es obligatoria");
        amount = Objects.requireNonNull(amount, "El monto es obligatorio");
    }
}

