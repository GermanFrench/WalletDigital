package com.tuusuario.wallet.interfaces.rest;

import com.tuusuario.wallet.application.usecase.CreateAccountUseCase;
import com.tuusuario.wallet.application.usecase.DepositMoneyUseCase;
import com.tuusuario.wallet.application.usecase.GetAccountUseCase;
import com.tuusuario.wallet.application.usecase.TransferMoneyUseCase;
import com.tuusuario.wallet.application.usecase.WithdrawMoneyUseCase;
import com.tuusuario.wallet.domain.enums.AccountStatus;
import com.tuusuario.wallet.domain.model.Account;
import com.tuusuario.wallet.interfaces.rest.request.CreateAccountRequest;
import com.tuusuario.wallet.interfaces.rest.request.DepositMoneyRequest;
import com.tuusuario.wallet.interfaces.rest.request.TransferMoneyRequest;
import com.tuusuario.wallet.interfaces.rest.request.WithdrawMoneyRequest;
import com.tuusuario.wallet.interfaces.rest.response.AccountResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    private static final String ACCOUNT_NUMBER = "0000000000000000000001";
    private static final String ACCOUNT_ALIAS = "jane.alias.1001";

    @Mock
    private CreateAccountUseCase createAccountUseCase;

    @Mock
    private DepositMoneyUseCase depositMoneyUseCase;

    @Mock
    private GetAccountUseCase getAccountUseCase;

    @Mock
    private WithdrawMoneyUseCase withdrawMoneyUseCase;

    @Mock
    private TransferMoneyUseCase transferMoneyUseCase;

    @InjectMocks
    private AccountController accountController;

    @Captor
    private ArgumentCaptor<UUID> uuidCaptor;

    @Test
    void shouldCreateAccount() {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Account account = Account.restore(
            accountId,
            userId,
            BigDecimal.ZERO,
            "USD",
            AccountStatus.ACTIVE,
            ACCOUNT_NUMBER,
            ACCOUNT_ALIAS
        );

        when(createAccountUseCase.createAccount(any())).thenReturn(account);

        AccountResponse response = accountController.createAccount(new CreateAccountRequest("Jane Doe", "jane@example.com", "USD"));

        assertEquals(accountId, response.id());
        assertEquals(userId, response.userId());
        assertEquals("USD", response.currency());
        assertEquals("ACTIVE", response.status());
        assertEquals(ACCOUNT_NUMBER, response.accountNumber());
        assertEquals(ACCOUNT_ALIAS, response.alias());
    }

    @Test
    void shouldDepositMoney() {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Account account = Account.restore(
            accountId,
            userId,
            new BigDecimal("150.00"),
            "USD",
            AccountStatus.ACTIVE,
            ACCOUNT_NUMBER,
            ACCOUNT_ALIAS
        );

        when(depositMoneyUseCase.depositMoney(eq(accountId), any())).thenReturn(account);

        AccountResponse response = accountController.depositMoney(accountId, new DepositMoneyRequest(new BigDecimal("50.00")));

        verify(depositMoneyUseCase).depositMoney(uuidCaptor.capture(), any());
        assertEquals(accountId, uuidCaptor.getValue());
        assertEquals(new BigDecimal("150.00"), response.balance());
    }

    @Test
    void shouldGetAccount() {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Account account = Account.restore(
            accountId,
            userId,
            new BigDecimal("42.00"),
            "USD",
            AccountStatus.ACTIVE,
            ACCOUNT_NUMBER,
            ACCOUNT_ALIAS
        );

        when(getAccountUseCase.getAccount(accountId)).thenReturn(account);

        AccountResponse response = accountController.getAccount(accountId);

        assertEquals(accountId, response.id());
        assertEquals(new BigDecimal("42.00"), response.balance());
    }

    @Test
    void shouldWithdrawMoney() {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Account account = Account.restore(
            accountId,
            userId,
            new BigDecimal("80.00"),
            "USD",
            AccountStatus.ACTIVE,
            ACCOUNT_NUMBER,
            ACCOUNT_ALIAS
        );

        when(withdrawMoneyUseCase.withdrawMoney(eq(accountId), any())).thenReturn(account);

        AccountResponse response = accountController.withdrawMoney(accountId, new WithdrawMoneyRequest(new BigDecimal("20.00")));

        assertEquals(new BigDecimal("80.00"), response.balance());
    }

    @Test
    void shouldTransferMoney() {
        UUID accountId = UUID.randomUUID();
        UUID destinationAccountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Account account = Account.restore(
            accountId,
            userId,
            new BigDecimal("70.00"),
            "USD",
            AccountStatus.ACTIVE,
            ACCOUNT_NUMBER,
            ACCOUNT_ALIAS
        );

        when(transferMoneyUseCase.transferMoney(eq(accountId), any())).thenReturn(account);

        AccountResponse response = accountController.transferMoney(
                accountId,
                new TransferMoneyRequest(destinationAccountId, new BigDecimal("30.00"))
        );

        assertEquals(accountId, response.id());
        assertEquals("USD", response.currency());
    }
}

