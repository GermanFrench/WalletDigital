package com.tuusuario.wallet.interfaces.rest.response;

import com.tuusuario.wallet.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO de salida para transacciones.
 */
public record TransactionResponse(
        UUID id,
        UUID accountId,
        BigDecimal amount,
        String type,
        String status,
        Instant timestamp,
        String description,
        UUID relatedAccountId
) {
    public static TransactionResponse from(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getAccountId(),
                tx.getAmount(),
                tx.getType().name(),
                tx.getStatus().name(),
                tx.getTimestamp(),
                tx.getDescription(),
                tx.getRelatedAccountId()
        );
    }
}
