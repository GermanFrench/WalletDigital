package com.tuusuario.wallet.application.usecase;

import com.tuusuario.wallet.domain.model.Account;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Datos para transferir dinero usando email o alias del destinatario.
 */
public record TransferByEmailCommand(String emailOrAlias, BigDecimal amount) {

    public TransferByEmailCommand {
        Objects.requireNonNull(emailOrAlias, "El email o alias destino es obligatorio");
        if (emailOrAlias.isBlank()) {
            throw new IllegalArgumentException("El email o alias destino no puede estar vacío");
        }
        Objects.requireNonNull(amount, "El monto es obligatorio");
    }
}
