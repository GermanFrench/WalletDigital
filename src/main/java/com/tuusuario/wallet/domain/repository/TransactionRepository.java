package com.tuusuario.wallet.domain.repository;

import com.tuusuario.wallet.domain.model.Transaction;

/**
 * Puerto de salida del dominio para transacciones.
 */
public interface TransactionRepository {

    Transaction save(Transaction transaction);
}

