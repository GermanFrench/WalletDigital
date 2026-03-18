package com.tuusuario.wallet.interfaces.rest;

import com.tuusuario.wallet.application.usecase.AuthenticateUserCommand;
import com.tuusuario.wallet.application.usecase.AuthenticateUserUseCase;
import com.tuusuario.wallet.interfaces.rest.request.LoginRequest;
import com.tuusuario.wallet.interfaces.rest.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST de autenticación.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Autenticación JWT")
public class AuthController {

    private final AuthenticateUserUseCase authenticateUserUseCase;

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario y emitir JWT")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return AuthResponse.from(authenticateUserUseCase.authenticate(
                new AuthenticateUserCommand(request.email(), request.password())
        ));
    }
}

