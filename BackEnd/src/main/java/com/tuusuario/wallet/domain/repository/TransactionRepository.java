package com.tuusuario.wallet.domain.repository;

import com.tuusuario.wallet.domain.model.Transaction;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de salida del dominio para transacciones.
 */
public interface TransactionRepository {

    Transaction save(Transaction transaction);

    List<Transaction> findByAccountId(UUID accountId, int page, int size);
}

