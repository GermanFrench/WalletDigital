package com.tuusuario.wallet.infrastructure.persistence;

import com.tuusuario.wallet.domain.model.Transaction;
import com.tuusuario.wallet.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA del repositorio de transacciones del dominio.
 */
@Repository
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepository {

    private final SpringDataTransactionRepository transactionRepository;
    private final SpringDataAccountRepository accountRepository;

    @Override
    public Transaction save(Transaction transaction) {
        AccountJpaEntity account = accountRepository.findById(transaction.getAccountId())
                .orElseThrow(() -> new IllegalStateException("No existe la cuenta asociada a la transacción"));

        AccountJpaEntity relatedAccount = Optional.ofNullable(transaction.getRelatedAccountId())
                .flatMap(accountRepository::findById)
                .orElse(null);

        TransactionJpaEntity entity = TransactionJpaEntity.builder()
                .id(transaction.getId())
                .account(account)
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .timestamp(transaction.getTimestamp())
                .description(transaction.getDescription())
                .relatedAccount(relatedAccount)
                .build();

        return toDomain(transactionRepository.save(entity));
    }

    @Override
    public List<Transaction> findByAccountId(UUID accountId, int page, int size) {
        return transactionRepository
                .findByAccountIdOrderByTimestampDesc(accountId, PageRequest.of(page, size))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Transaction toDomain(TransactionJpaEntity entity) {
        return Transaction.restore(
                entity.getId(),
                entity.getAccount().getId(),
                entity.getAmount(),
                entity.getType(),
                entity.getStatus(),
                entity.getTimestamp(),
                entity.getDescription(),
                entity.getRelatedAccount() != null ? entity.getRelatedAccount().getId() : null
        );
    }
}

