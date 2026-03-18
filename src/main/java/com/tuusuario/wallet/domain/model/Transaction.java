package com.tuusuario.wallet.domain.model;

import com.tuusuario.wallet.domain.enums.TransactionStatus;
import com.tuusuario.wallet.domain.enums.TransactionType;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio que registra un movimiento financiero.
 */
@Getter
@EqualsAndHashCode(of = "id")
public final class Transaction {

    private static final int MONEY_SCALE = 2;

    private final UUID id;
    private final UUID accountId;
    private final BigDecimal amount;
    private final TransactionType type;
    private final TransactionStatus status;
    private final Instant timestamp;

    private Transaction(UUID id,
                        UUID accountId,
                        BigDecimal amount,
                        TransactionType type,
                        TransactionStatus status,
                        Instant timestamp) {
        this.id = Objects.requireNonNull(id, "El id de la transacción es obligatorio");
        this.accountId = Objects.requireNonNull(accountId, "El id de la cuenta es obligatorio");
        this.amount = normalizeAmount(amount);
        this.type = Objects.requireNonNull(type, "El tipo de transacción es obligatorio");
        this.status = Objects.requireNonNull(status, "El estado de la transacción es obligatorio");
        this.timestamp = Objects.requireNonNull(timestamp, "La fecha de transacción es obligatoria");
    }

    public static Transaction credit(UUID id, UUID accountId, BigDecimal amount, Instant timestamp) {
        return new Transaction(id, accountId, amount, TransactionType.CREDIT, TransactionStatus.COMPLETED, timestamp);
    }

    public static Transaction debit(UUID id, UUID accountId, BigDecimal amount, Instant timestamp) {
        return new Transaction(id, accountId, amount, TransactionType.DEBIT, TransactionStatus.COMPLETED, timestamp);
    }

    public static Transaction restore(UUID id,
                                      UUID accountId,
                                      BigDecimal amount,
                                      TransactionType type,
                                      TransactionStatus status,
                                      Instant timestamp) {
        return new Transaction(id, accountId, amount, type, status, timestamp);
    }

    private static BigDecimal normalizeAmount(BigDecimal value) {
        BigDecimal normalized = Objects.requireNonNull(value, "El monto es obligatorio")
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        if (normalized.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto de la transacción debe ser mayor que cero");
        }
        return normalized;
    }
}

