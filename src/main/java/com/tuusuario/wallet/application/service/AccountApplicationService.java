package com.tuusuario.wallet.application.service;

import com.tuusuario.wallet.application.usecase.CreateAccountCommand;
import com.tuusuario.wallet.application.usecase.CreateAccountUseCase;
import com.tuusuario.wallet.application.usecase.DepositMoneyCommand;
import com.tuusuario.wallet.application.usecase.DepositMoneyUseCase;
import com.tuusuario.wallet.application.usecase.GetAccountUseCase;
import com.tuusuario.wallet.application.usecase.TransferMoneyCommand;
import com.tuusuario.wallet.application.usecase.TransferMoneyUseCase;
import com.tuusuario.wallet.application.usecase.WithdrawMoneyCommand;
import com.tuusuario.wallet.application.usecase.WithdrawMoneyUseCase;
import com.tuusuario.wallet.domain.model.Account;
import com.tuusuario.wallet.domain.model.Transaction;
import com.tuusuario.wallet.domain.model.User;
import com.tuusuario.wallet.domain.repository.AccountRepository;
import com.tuusuario.wallet.domain.repository.TransactionRepository;
import com.tuusuario.wallet.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Servicio de aplicación que orquesta los casos de uso de cuentas.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AccountApplicationService implements CreateAccountUseCase, DepositMoneyUseCase, GetAccountUseCase,
        WithdrawMoneyUseCase, TransferMoneyUseCase {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final Clock clock;

    @Override
    public Account createAccount(CreateAccountCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseGet(() -> userRepository.save(User.create(UUID.randomUUID(), command.name(), command.email())));

        Account account = Account.open(UUID.randomUUID(), user.getId(), command.currency());
        return accountRepository.save(account);
    }

    @Override
    public Account depositMoney(UUID accountId, DepositMoneyCommand command) {
        Account account = getAccount(accountId);

        account.deposit(command.amount());
        Account updatedAccount = accountRepository.save(account);

        registerCredit(updatedAccount.getId(), command.amount());

        return updatedAccount;
    }

    @Override
    @Transactional(readOnly = true)
    public Account getAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada: " + accountId));
    }

    @Override
    public Account withdrawMoney(UUID accountId, WithdrawMoneyCommand command) {
        Account account = getAccount(accountId);

        account.withdraw(command.amount());
        Account updatedAccount = accountRepository.save(account);

        registerDebit(updatedAccount.getId(), command.amount());
        return updatedAccount;
    }

    @Override
    public Account transferMoney(UUID sourceAccountId, TransferMoneyCommand command) {
        if (sourceAccountId.equals(command.destinationAccountId())) {
            throw new IllegalArgumentException("La cuenta origen y destino no pueden ser la misma");
        }

        Account sourceAccount = getAccount(sourceAccountId);
        Account destinationAccount = getAccount(command.destinationAccountId());
        sourceAccount.validateSameCurrency(destinationAccount);

        sourceAccount.withdraw(command.amount());
        destinationAccount.deposit(command.amount());

        Account updatedSourceAccount = accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        registerDebit(updatedSourceAccount.getId(), command.amount());
        registerCredit(destinationAccount.getId(), command.amount());

        return updatedSourceAccount;
    }

    private void registerCredit(UUID accountId, java.math.BigDecimal amount) {
        transactionRepository.save(Transaction.credit(
                UUID.randomUUID(),
                accountId,
                amount,
                Instant.now(clock)
        ));
    }

    private void registerDebit(UUID accountId, java.math.BigDecimal amount) {
        transactionRepository.save(Transaction.debit(
                UUID.randomUUID(),
                accountId,
                amount,
                Instant.now(clock)
        ));
    }
}

