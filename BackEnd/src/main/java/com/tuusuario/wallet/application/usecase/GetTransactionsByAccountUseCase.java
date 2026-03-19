package com.tuusuario.wallet.application.usecase;

import com.tuusuario.wallet.domain.model.Transaction;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso: listar transacciones de una cuenta con paginación.
 */
public interface GetTransactionsByAccountUseCase {

    List<Transaction> getTransactionsByAccount(UUID accountId, int page, int size);
}
