package com.tuusuario.wallet.application.usecase;

public interface RegisterUserUseCase {

    AuthenticationResult register(RegisterUserCommand command);
}
