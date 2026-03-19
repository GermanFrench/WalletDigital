package com.tuusuario.wallet.interfaces.rest;

import com.tuusuario.wallet.application.usecase.CreateAccountCommand;
import com.tuusuario.wallet.application.usecase.CreateAccountUseCase;
import com.tuusuario.wallet.application.usecase.DepositMoneyCommand;
import com.tuusuario.wallet.application.usecase.DepositMoneyUseCase;
import com.tuusuario.wallet.application.usecase.GetAccountByEmailUseCase;
import com.tuusuario.wallet.application.usecase.GetAccountUseCase;
import com.tuusuario.wallet.application.usecase.GetTransactionsByAccountUseCase;
import com.tuusuario.wallet.application.usecase.TransferByEmailCommand;
import com.tuusuario.wallet.application.usecase.TransferByEmailUseCase;
import com.tuusuario.wallet.application.usecase.TransferMoneyCommand;
import com.tuusuario.wallet.application.usecase.TransferMoneyUseCase;
import com.tuusuario.wallet.application.usecase.WithdrawMoneyCommand;
import com.tuusuario.wallet.application.usecase.WithdrawMoneyUseCase;
import com.tuusuario.wallet.interfaces.rest.request.CreateAccountRequest;
import com.tuusuario.wallet.interfaces.rest.request.DepositMoneyRequest;
import com.tuusuario.wallet.interfaces.rest.request.TransferByEmailRequest;
import com.tuusuario.wallet.interfaces.rest.request.TransferMoneyRequest;
import com.tuusuario.wallet.interfaces.rest.request.WithdrawMoneyRequest;
import com.tuusuario.wallet.interfaces.rest.response.AccountResponse;
import com.tuusuario.wallet.interfaces.rest.response.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para operaciones de cuentas.
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Validated
@Tag(name = "Accounts", description = "Operaciones de cuentas de wallet")
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final GetAccountByEmailUseCase getAccountByEmailUseCase;
    private final DepositMoneyUseCase depositMoneyUseCase;
    private final WithdrawMoneyUseCase withdrawMoneyUseCase;
    private final TransferMoneyUseCase transferMoneyUseCase;
    private final TransferByEmailUseCase transferByEmailUseCase;
    private final GetTransactionsByAccountUseCase getTransactionsByAccountUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una nueva cuenta")
    public AccountResponse createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return AccountResponse.from(createAccountUseCase.createAccount(
                new CreateAccountCommand(request.name(), request.email(), request.currency())
        ));
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener la cuenta del usuario autenticado")
    public ResponseEntity<AccountResponse> getMyAccount(Authentication authentication) {
        String email = authentication.getName();
        return getAccountByEmailUseCase.findAccountByEmail(email)
                .map(account -> ResponseEntity.ok(AccountResponse.from(account)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Consultar una cuenta y su saldo")
    public AccountResponse getAccount(@PathVariable UUID accountId) {
        return AccountResponse.from(getAccountUseCase.getAccount(accountId));
    }

    @GetMapping("/{accountId}/transactions")
    @Operation(summary = "Listar transacciones de una cuenta (más recientes primero)")
    public List<TransactionResponse> getTransactions(
            @PathVariable UUID accountId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return getTransactionsByAccountUseCase
                .getTransactionsByAccount(accountId, page, size)
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @PostMapping("/{accountId}/deposit")
    @Operation(summary = "Depositar dinero en una cuenta")
    public AccountResponse depositMoney(@PathVariable UUID accountId,
                                        @Valid @RequestBody DepositMoneyRequest request) {
        return AccountResponse.from(depositMoneyUseCase.depositMoney(
                accountId, new DepositMoneyCommand(request.amount())
        ));
    }

    @PostMapping("/{accountId}/withdraw")
    @Operation(summary = "Retirar dinero de una cuenta")
    public AccountResponse withdrawMoney(@PathVariable UUID accountId,
                                         @Valid @RequestBody WithdrawMoneyRequest request) {
        return AccountResponse.from(withdrawMoneyUseCase.withdrawMoney(
                accountId, new WithdrawMoneyCommand(request.amount())
        ));
    }

    @PostMapping("/{accountId}/transfer")
    @Operation(summary = "Transferir dinero a otra cuenta por ID")
    public AccountResponse transferMoney(@PathVariable UUID accountId,
                                         @Valid @RequestBody TransferMoneyRequest request) {
        return AccountResponse.from(transferMoneyUseCase.transferMoney(
                accountId, new TransferMoneyCommand(request.destinationAccountId(), request.amount())
        ));
    }

    @PostMapping("/{accountId}/transfer-by-email")
    @Operation(summary = "Transferir dinero usando email, alias o número de cuenta del destinatario")
    public AccountResponse transferByEmail(@PathVariable UUID accountId,
                                           @Valid @RequestBody TransferByEmailRequest request) {
        return AccountResponse.from(transferByEmailUseCase.transferByEmail(
                accountId, new TransferByEmailCommand(request.emailOrAlias(), request.amount())
        ));
    }
}

