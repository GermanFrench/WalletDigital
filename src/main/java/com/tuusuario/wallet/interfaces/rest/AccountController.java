package com.tuusuario.wallet.interfaces.rest;

import com.tuusuario.wallet.application.usecase.CreateAccountCommand;
import com.tuusuario.wallet.application.usecase.CreateAccountUseCase;
import com.tuusuario.wallet.application.usecase.DepositMoneyCommand;
import com.tuusuario.wallet.application.usecase.DepositMoneyUseCase;
import com.tuusuario.wallet.application.usecase.GetAccountUseCase;
import com.tuusuario.wallet.application.usecase.TransferMoneyCommand;
import com.tuusuario.wallet.application.usecase.TransferMoneyUseCase;
import com.tuusuario.wallet.application.usecase.WithdrawMoneyCommand;
import com.tuusuario.wallet.application.usecase.WithdrawMoneyUseCase;
import com.tuusuario.wallet.interfaces.rest.request.CreateAccountRequest;
import com.tuusuario.wallet.interfaces.rest.request.DepositMoneyRequest;
import com.tuusuario.wallet.interfaces.rest.request.TransferMoneyRequest;
import com.tuusuario.wallet.interfaces.rest.request.WithdrawMoneyRequest;
import com.tuusuario.wallet.interfaces.rest.response.AccountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controlador REST para operaciones básicas de cuentas.
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Operaciones de cuentas de wallet")
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final DepositMoneyUseCase depositMoneyUseCase;
    private final WithdrawMoneyUseCase withdrawMoneyUseCase;
    private final TransferMoneyUseCase transferMoneyUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una nueva cuenta")
    public AccountResponse createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return AccountResponse.from(createAccountUseCase.createAccount(
                new CreateAccountCommand(request.name(), request.email(), request.currency())
        ));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Consultar una cuenta y su saldo")
    public AccountResponse getAccount(@PathVariable UUID accountId) {
        return AccountResponse.from(getAccountUseCase.getAccount(accountId));
    }

    @PostMapping("/{accountId}/deposit")
    @Operation(summary = "Depositar dinero en una cuenta")
    public AccountResponse depositMoney(@PathVariable UUID accountId,
                                        @Valid @RequestBody DepositMoneyRequest request) {
        return AccountResponse.from(depositMoneyUseCase.depositMoney(
                accountId,
                new DepositMoneyCommand(request.amount())
        ));
    }

    @PostMapping("/{accountId}/withdraw")
    @Operation(summary = "Retirar dinero de una cuenta")
    public AccountResponse withdrawMoney(@PathVariable UUID accountId,
                                         @Valid @RequestBody WithdrawMoneyRequest request) {
        return AccountResponse.from(withdrawMoneyUseCase.withdrawMoney(
                accountId,
                new WithdrawMoneyCommand(request.amount())
        ));
    }

    @PostMapping("/{accountId}/transfer")
    @Operation(summary = "Transferir dinero a otra cuenta")
    public AccountResponse transferMoney(@PathVariable UUID accountId,
                                         @Valid @RequestBody TransferMoneyRequest request) {
        return AccountResponse.from(transferMoneyUseCase.transferMoney(
                accountId,
                new TransferMoneyCommand(request.destinationAccountId(), request.amount())
        ));
    }
}

