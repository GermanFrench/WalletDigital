package com.tuusuario.wallet.application.usecase;

import com.tuusuario.wallet.domain.model.Account;

import java.util.UUID;

/**
 * Caso de uso: transferir dinero usando email o alias del destinatario.
 */
public interface TransferByEmailUseCase {

    Account transferByEmail(UUID sourceAccountId, TransferByEmailCommand command);
}
