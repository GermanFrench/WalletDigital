package com.tuusuario.wallet.application.service;

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
import com.tuusuario.wallet.domain.model.Account;
import com.tuusuario.wallet.domain.model.Transaction;
import com.tuusuario.wallet.domain.model.User;
import com.tuusuario.wallet.domain.repository.AccountRepository;
import com.tuusuario.wallet.domain.repository.TransactionRepository;
import com.tuusuario.wallet.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de aplicación que orquesta los casos de uso de cuentas.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AccountApplicationService implements
        CreateAccountUseCase,
        DepositMoneyUseCase,
        GetAccountUseCase,
        GetAccountByEmailUseCase,
        WithdrawMoneyUseCase,
        TransferMoneyUseCase,
        TransferByEmailUseCase,
        GetTransactionsByAccountUseCase {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final Clock clock;

    @Override
    public Account createAccount(CreateAccountCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseGet(() -> userRepository.save(User.create(UUID.randomUUID(), command.name(), command.email())));

        String accountNumber = generateAccountNumber();
        String alias = generateAlias(command.email(), command.name());

        Account account = Account.open(UUID.randomUUID(), user.getId(), command.currency(), accountNumber, alias);
        return accountRepository.save(account);
    }

    @Override
    public Account depositMoney(UUID accountId, DepositMoneyCommand command) {
        Account account = getAccount(accountId);
        account.deposit(command.amount());
        Account updated = accountRepository.save(account);
        registerCredit(updated.getId(), command.amount(), "Depósito", null);
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public Account getAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada: " + accountId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    @Override
    public Account withdrawMoney(UUID accountId, WithdrawMoneyCommand command) {
        Account account = getAccount(accountId);
        account.withdraw(command.amount());
        Account updated = accountRepository.save(account);
        registerDebit(updated.getId(), command.amount(), "Retiro", null);
        return updated;
    }

    @Override
    public Account transferMoney(UUID sourceAccountId, TransferMoneyCommand command) {
        if (sourceAccountId.equals(command.destinationAccountId())) {
            throw new IllegalArgumentException("La cuenta origen y destino no pueden ser la misma");
        }
        Account source = getAccount(sourceAccountId);
        Account destination = getAccount(command.destinationAccountId());
        source.validateSameCurrency(destination);

        source.withdraw(command.amount());
        destination.deposit(command.amount());

        Account updatedSource = accountRepository.save(source);
        accountRepository.save(destination);

        registerDebit(updatedSource.getId(), command.amount(), "Transferencia enviada", destination.getId());
        registerCredit(destination.getId(), command.amount(), "Transferencia recibida", updatedSource.getId());

        return updatedSource;
    }

    @Override
    public Account transferByEmail(UUID sourceAccountId, TransferByEmailCommand command) {
        String target = command.emailOrAlias().trim();

        Account destination = accountRepository.findByEmail(target)
                .or(() -> accountRepository.findByAlias(target))
                .or(() -> accountRepository.findByAccountNumber(target))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró ninguna cuenta para: " + target));

        if (sourceAccountId.equals(destination.getId())) {
            throw new IllegalArgumentException("No puedes transferirte dinero a tu misma cuenta");
        }

        Account source = getAccount(sourceAccountId);
        source.validateSameCurrency(destination);

        source.withdraw(command.amount());
        destination.deposit(command.amount());

        Account updatedSource = accountRepository.save(source);
        accountRepository.save(destination);

        registerDebit(updatedSource.getId(), command.amount(), "Transferencia a " + target, destination.getId());
        registerCredit(destination.getId(), command.amount(), "Transferencia recibida de cuenta " + sourceAccountId, updatedSource.getId());

        return updatedSource;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByAccount(UUID accountId, int page, int size) {
        // Verificar que la cuenta existe
        getAccount(accountId);
        return transactionRepository.findByAccountId(accountId, page, size);
    }

    // ── Helpers privados ─────────────────────────────────────────────────────

    private void registerCredit(UUID accountId, BigDecimal amount, String description, UUID relatedAccountId) {
        transactionRepository.save(Transaction.credit(
                UUID.randomUUID(), accountId, amount, Instant.now(clock), description, relatedAccountId));
    }

    private void registerDebit(UUID accountId, BigDecimal amount, String description, UUID relatedAccountId) {
        transactionRepository.save(Transaction.debit(
                UUID.randomUUID(), accountId, amount, Instant.now(clock), description, relatedAccountId));
    }

    private static String generateAccountNumber() {
        // Formato tipo CVU: 22 dígitos
        long base = 1_000_000_000_000_000_000L;
        long random = Math.abs(RANDOM.nextLong() % 8_000_000_000_000_000_000L);
        return String.format("%022d", base + (random % base));
    }

    private static String generateAlias(String email, String name) {
        String prefix = email.contains("@")
                ? email.substring(0, email.indexOf('@')).toLowerCase().replaceAll("[^a-z0-9]", "")
                : name.toLowerCase().replaceAll("[^a-z0-9]", "");
        if (prefix.isBlank()) prefix = "wallet";
        int suffix = 1000 + RANDOM.nextInt(8999);
        return prefix + "." + suffix;
    }
}

