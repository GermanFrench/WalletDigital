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
                    return existing;
                })
                .orElseGet(() -> AccountJpaEntity.builder()
                        .id(account.getId())
                        .user(user)
                        .balance(account.getBalance())
                        .currency(account.getCurrency())
                        .status(account.getStatus())
                        .build());

        return toDomain(accountRepository.save(entity));
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return accountRepository.findById(id).map(this::toDomain);
    }

    private Account toDomain(AccountJpaEntity entity) {
        return Account.restore(
                entity.getId(),
                entity.getUser().getId(),
                entity.getBalance(),
                entity.getCurrency(),
                entity.getStatus()
        );
    }
}

