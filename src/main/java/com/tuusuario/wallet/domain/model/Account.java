package com.tuusuario.wallet.domain.model;

import com.tuusuario.wallet.domain.enums.AccountStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio que representa una cuenta monetaria.
 */
@Getter
@EqualsAndHashCode(of = "id")
public final class Account {

    private static final int MONEY_SCALE = 2;

    private final UUID id;
    private final UUID userId;
    private BigDecimal balance;
    private final String currency;
    private AccountStatus status;

    private Account(UUID id, UUID userId, BigDecimal balance, String currency, AccountStatus status) {
        this.id = Objects.requireNonNull(id, "El id de la cuenta es obligatorio");
        this.userId = Objects.requireNonNull(userId, "El id del usuario es obligatorio");
        this.balance = normalizeBalance(balance);
        this.currency = normalizeCurrency(currency);
        this.status = Objects.requireNonNull(status, "El estado de la cuenta es obligatorio");
    }

    public static Account open(UUID id, UUID userId, String currency) {
        return new Account(id, userId, BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP), currency, AccountStatus.ACTIVE);
    }

    public static Account restore(UUID id, UUID userId, BigDecimal balance, String currency, AccountStatus status) {
        return new Account(id, userId, balance, currency, status);
    }

    public void deposit(BigDecimal amount) {
        ensureAccountCanOperate();
        BigDecimal normalizedAmount = normalizePositiveAmount(amount);
        this.balance = this.balance.add(normalizedAmount);
    }

    public void withdraw(BigDecimal amount) {
        ensureAccountCanOperate();
        BigDecimal normalizedAmount = normalizePositiveAmount(amount);
        if (this.balance.compareTo(normalizedAmount) < 0) {
            throw new IllegalStateException("Fondos insuficientes para completar la operación");
        }
        this.balance = this.balance.subtract(normalizedAmount);
    }

    public void validateSameCurrency(Account targetAccount) {
        Objects.requireNonNull(targetAccount, "La cuenta destino es obligatoria");
        if (!this.currency.equals(targetAccount.currency)) {
            throw new IllegalArgumentException("Las cuentas deben operar en la misma moneda");
        }
    }

    private void ensureAccountCanOperate() {
        if (status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("La cuenta no se encuentra activa para operar");
        }
    }

    private static BigDecimal normalizeBalance(BigDecimal amount) {
        BigDecimal normalized = Objects.requireNonNull(amount, "El saldo es obligatorio")
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        if (normalized.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El saldo no puede ser negativo");
        }
        return normalized;
    }

    private static BigDecimal normalizePositiveAmount(BigDecimal amount) {
        BigDecimal normalized = normalizeBalance(amount);
        if (normalized.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero");
        }
        return normalized;
    }

    private static String normalizeCurrency(String value) {
        String normalized = Objects.requireNonNull(value, "La moneda es obligatoria").trim().toUpperCase();
        if (!normalized.matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("La moneda debe cumplir el formato ISO de 3 letras");
        }
        return normalized;
    }
}

