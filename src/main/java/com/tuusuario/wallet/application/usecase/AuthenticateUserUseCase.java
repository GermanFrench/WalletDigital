package com.tuusuario.wallet.application.usecase;

public interface AuthenticateUserUseCase {

    AuthenticationResult authenticate(AuthenticateUserCommand command);
}

