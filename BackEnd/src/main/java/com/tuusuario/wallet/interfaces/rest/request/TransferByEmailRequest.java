package com.tuusuario.wallet.interfaces.rest.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request para transferir usando email, alias o número de cuenta del destinatario.
 */
public record TransferByEmailRequest(
        @NotBlank String emailOrAlias,
        @NotNull @DecimalMin("0.01") BigDecimal amount
) {
}
