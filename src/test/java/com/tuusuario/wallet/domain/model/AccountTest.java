package com.tuusuario.wallet.domain.model;

import com.tuusuario.wallet.domain.enums.AccountStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountTest {

    @Test
    void shouldDepositMoneyIntoAnActiveAccount() {
        Account account = Account.restore(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("10.00"),
                "USD",
                AccountStatus.ACTIVE
        );

        account.deposit(new BigDecimal("15.75"));

        assertEquals(new BigDecimal("25.75"), account.getBalance());
    }

    @Test
    void shouldRejectDepositWhenAmountIsZero() {
        Account account = Account.open(UUID.randomUUID(), UUID.randomUUID(), "USD");

        assertThrows(IllegalArgumentException.class, () -> account.deposit(BigDecimal.ZERO));
    }

    @Test
    void shouldWithdrawMoneyFromAnActiveAccount() {
        Account account = Account.restore(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "USD",
                AccountStatus.ACTIVE
        );

        account.withdraw(new BigDecimal("40.50"));

        assertEquals(new BigDecimal("59.50"), account.getBalance());
    }

    @Test
    void shouldRejectWithdrawWhenFundsAreInsufficient() {
        Account account = Account.restore(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("25.00"),
                "USD",
                AccountStatus.ACTIVE
        );

        assertThrows(IllegalStateException.class, () -> account.withdraw(new BigDecimal("30.00")));
    }
}

