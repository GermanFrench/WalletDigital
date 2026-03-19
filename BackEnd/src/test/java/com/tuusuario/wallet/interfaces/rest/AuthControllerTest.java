package com.tuusuario.wallet.interfaces.rest;

import com.tuusuario.wallet.application.usecase.AuthenticateUserCommand;
import com.tuusuario.wallet.application.usecase.AuthenticateUserUseCase;
import com.tuusuario.wallet.application.usecase.AuthenticationResult;
import com.tuusuario.wallet.application.usecase.LogoutUseCase;
import com.tuusuario.wallet.application.usecase.RefreshAccessTokenUseCase;
import com.tuusuario.wallet.application.usecase.RegisterUserUseCase;
import com.tuusuario.wallet.application.usecase.RequestPasswordResetUseCase;
import com.tuusuario.wallet.application.usecase.ResetPasswordUseCase;
import com.tuusuario.wallet.interfaces.rest.request.ForgotPasswordRequest;
import com.tuusuario.wallet.interfaces.rest.request.LoginRequest;
import com.tuusuario.wallet.interfaces.rest.request.RefreshTokenRequest;
import com.tuusuario.wallet.interfaces.rest.request.RegisterRequest;
import com.tuusuario.wallet.interfaces.rest.request.ResetPasswordRequest;
import com.tuusuario.wallet.interfaces.rest.response.AuthResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticateUserUseCase authenticateUserUseCase;

    @Mock
    private RegisterUserUseCase registerUserUseCase;

    @Mock
    private RefreshAccessTokenUseCase refreshAccessTokenUseCase;

    @Mock
    private LogoutUseCase logoutUseCase;

    @Mock
    private RequestPasswordResetUseCase requestPasswordResetUseCase;

    @Mock
    private ResetPasswordUseCase resetPasswordUseCase;

    @InjectMocks
    private AuthController authController;

    @Test
    void shouldLoginAndReturnJwt() {
        when(authenticateUserUseCase.authenticate(any()))
                .thenReturn(new AuthenticationResult("token.jwt.value", "Bearer", 3600, "refresh-token"));

        AuthResponse response = authController.login(new LoginRequest("admin@wallet.local", "ChangeMe123!"));

        assertEquals("token.jwt.value", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600, response.expiresInSeconds());
        assertEquals("refresh-token", response.refreshToken());
    }

    @Test
    void shouldRegisterAndReturnJwt() {
        when(registerUserUseCase.register(any()))
                .thenReturn(new AuthenticationResult("register.jwt", "Bearer", 1800, "register-refresh"));

        AuthResponse response = authController.register(new RegisterRequest("new@wallet.local", "ChangeMe123!"));

        assertEquals("register.jwt", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(1800, response.expiresInSeconds());
        assertEquals("register-refresh", response.refreshToken());
    }

    @Test
    void shouldRefreshAndReturnNewAccessToken() {
        when(refreshAccessTokenUseCase.refresh(eq("old-refresh")))
                .thenReturn(new AuthenticationResult("new.jwt", "Bearer", 3600, "new-refresh"));

        AuthResponse response = authController.refresh(new RefreshTokenRequest("old-refresh"));

        assertEquals("new.jwt", response.accessToken());
        assertEquals("new-refresh", response.refreshToken());
    }

    @Test
    void shouldLogoutAndReturnNoContent() {
        ResponseEntity<Void> response = authController.logout(new RefreshTokenRequest("refresh-to-revoke"));

        verify(logoutUseCase).logout("refresh-to-revoke");
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void shouldReturnResetTokenInForgotPasswordWhenUseCaseProvidesOne() {
        when(requestPasswordResetUseCase.requestPasswordReset(eq("user@wallet.local")))
                .thenReturn("debug-reset-token");

        ResponseEntity<Map<String, String>> response =
                authController.forgotPassword(new ForgotPasswordRequest("user@wallet.local"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Si el email existe, recibirás instrucciones de recuperación", response.getBody().get("message"));
        assertEquals("debug-reset-token", response.getBody().get("resetToken"));
    }

    @Test
    void shouldHideResetTokenInForgotPasswordWhenUseCaseReturnsBlank() {
        when(requestPasswordResetUseCase.requestPasswordReset(eq("user@wallet.local")))
                .thenReturn("");

        ResponseEntity<Map<String, String>> response =
                authController.forgotPassword(new ForgotPasswordRequest("user@wallet.local"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Si el email existe, recibirás instrucciones de recuperación", response.getBody().get("message"));
        assertFalse(response.getBody().containsKey("resetToken"));
    }

    @Test
    void shouldResetPasswordAndReturnSuccessMessage() {
        ResponseEntity<Map<String, String>> response =
                authController.resetPassword(new ResetPasswordRequest("token-123", "NewPass123"));

        verify(resetPasswordUseCase).resetPassword("token-123", "NewPass123");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().containsKey("message"));
        assertEquals("Contraseña restablecida exitosamente", response.getBody().get("message"));
    }
}

