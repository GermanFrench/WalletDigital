package com.tuusuario.wallet.application.service;

import com.tuusuario.wallet.application.usecase.AuthenticateUserCommand;
import com.tuusuario.wallet.application.usecase.AuthenticateUserUseCase;
import com.tuusuario.wallet.application.usecase.AuthenticationResult;
import com.tuusuario.wallet.application.usecase.TokenProvider;
import com.tuusuario.wallet.domain.model.AuthUser;
import com.tuusuario.wallet.domain.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación para autenticación.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthenticationApplicationService implements AuthenticateUserUseCase {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Override
    public AuthenticationResult authenticate(AuthenticateUserCommand command) {
        AuthUser authUser = authUserRepository.findByEmail(command.email())
                .filter(AuthUser::isEnabled)
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(command.password(), authUser.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        return new AuthenticationResult(
                tokenProvider.generateToken(authUser),
                "Bearer",
                tokenProvider.getExpirationInSeconds()
        );
    }
}

