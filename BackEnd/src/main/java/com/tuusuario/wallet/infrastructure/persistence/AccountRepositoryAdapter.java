package com.tuusuario.wallet.infrastructure.persistence;

import com.tuusuario.wallet.domain.model.Account;
import com.tuusuario.wallet.domain.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA del repositorio de cuentas del dominio.
 */
@Repository
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {

    private final SpringDataAccountRepository accountRepository;
    private final SpringDataUserRepository userRepository;

    @Override
    public Account save(Account account) {
        UserJpaEntity user = userRepository.findById(account.getUserId())
                .orElseThrow(() -> new IllegalStateException("No existe el usuario asociado a la cuenta"));

        AccountJpaEntity entity = accountRepository.findById(account.getId())
                .map(existing -> {
                    existing.setUser(user);
                    existing.setBalance(account.getBalance());
                    existing.setCurrency(account.getCurrency());
                    existing.setStatus(account.getStatus());
                    existing.setAccountNumber(account.getAccountNumber());
                    existing.setAlias(account.getAlias());
                    return existing;
                })
                .orElseGet(() -> AccountJpaEntity.builder()
                        .id(account.getId())
                        .user(user)
                        .balance(account.getBalance())
                        .currency(account.getCurrency())
                        .status(account.getStatus())
                        .accountNumber(account.getAccountNumber())
                        .alias(account.getAlias())
                        .build());

        return toDomain(accountRepository.save(entity));
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return accountRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        return accountRepository.findFirstByUserEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<Account> findByAlias(String alias) {
        return accountRepository.findByAlias(alias).map(this::toDomain);
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber).map(this::toDomain);
    }

    private Account toDomain(AccountJpaEntity entity) {
        return Account.restore(
                entity.getId(),
                entity.getUser().getId(),
                entity.getBalance(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getAccountNumber(),
                entity.getAlias()
        );
    }
}

