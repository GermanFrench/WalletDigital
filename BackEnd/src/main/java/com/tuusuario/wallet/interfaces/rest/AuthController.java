package com.tuusuario.wallet.interfaces.rest;

import com.tuusuario.wallet.application.usecase.AuthenticateUserCommand;
import com.tuusuario.wallet.application.usecase.AuthenticateUserUseCase;
import com.tuusuario.wallet.application.usecase.LogoutUseCase;
import com.tuusuario.wallet.application.usecase.RefreshAccessTokenUseCase;
import com.tuusuario.wallet.application.usecase.RegisterUserCommand;
import com.tuusuario.wallet.application.usecase.RegisterUserUseCase;
import com.tuusuario.wallet.application.usecase.RequestPasswordResetUseCase;
import com.tuusuario.wallet.application.usecase.ResetPasswordUseCase;
import com.tuusuario.wallet.interfaces.rest.request.ForgotPasswordRequest;
import com.tuusuario.wallet.interfaces.rest.request.LoginRequest;
import com.tuusuario.wallet.interfaces.rest.request.RefreshTokenRequest;
import com.tuusuario.wallet.interfaces.rest.request.RegisterRequest;
import com.tuusuario.wallet.interfaces.rest.request.ResetPasswordRequest;
import com.tuusuario.wallet.interfaces.rest.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST de autenticación.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Autenticación JWT")
public class AuthController {

    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    private final RefreshAccessTokenUseCase refreshAccessTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;

    @PostMapping("/register")
    @Operation(summary = "Registrar usuario y emitir JWT + refresh token")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return AuthResponse.from(registerUserUseCase.register(
                new RegisterUserCommand(request.email(), request.password())
        ));
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario y emitir JWT + refresh token")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return AuthResponse.from(authenticateUserUseCase.authenticate(
                new AuthenticateUserCommand(request.email(), request.password())
        ));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Obtener nuevo access token usando un refresh token")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return AuthResponse.from(refreshAccessTokenUseCase.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revocar refresh token (cerrar sesión)")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        logoutUseCase.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar recuperación de contraseña")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String token = requestPasswordResetUseCase.requestPasswordReset(request.email());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Si el email existe, recibirás instrucciones de recuperación");
        if (!token.isBlank()) {
            response.put("resetToken", token);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña con token de recuperación")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordUseCase.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Contraseña restablecida exitosamente"));
    }
}

