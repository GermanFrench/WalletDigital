package com.tuusuario.wallet.interfaces.rest.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request HTTP para transferencias.
 */
public record TransferMoneyRequest(
        @NotNull(message = "La cuenta destino es obligatoria")
        UUID destinationAccountId,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero")
        BigDecimal amount
) {
}

