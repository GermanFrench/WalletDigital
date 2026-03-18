package com.tuusuario.wallet.infrastructure.persistence;

import com.tuusuario.wallet.domain.model.Transaction;
import com.tuusuario.wallet.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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

        TransactionJpaEntity entity = TransactionJpaEntity.builder()
                .id(transaction.getId())
                .account(account)
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .timestamp(transaction.getTimestamp())
                .build();

        return toDomain(transactionRepository.save(entity));
    }

    private Transaction toDomain(TransactionJpaEntity entity) {
        return Transaction.restore(
                entity.getId(),
                entity.getAccount().getId(),
                entity.getAmount(),
                entity.getType(),
                entity.getStatus(),
                entity.getTimestamp()
        );
    }
}

