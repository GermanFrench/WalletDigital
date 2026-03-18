package com.tuusuario.wallet.interfaces.rest;

import com.tuusuario.wallet.application.usecase.AuthenticateUserUseCase;
import com.tuusuario.wallet.application.usecase.AuthenticationResult;
import com.tuusuario.wallet.interfaces.rest.request.LoginRequest;
import com.tuusuario.wallet.interfaces.rest.response.AuthResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticateUserUseCase authenticateUserUseCase;

    @InjectMocks
    private AuthController authController;

    @Test
    void shouldLoginAndReturnJwt() {
        when(authenticateUserUseCase.authenticate(any()))
                .thenReturn(new AuthenticationResult("token.jwt.value", "Bearer", 3600));

        AuthResponse response = authController.login(new LoginRequest("admin@wallet.local", "ChangeMe123!"));

        assertEquals("token.jwt.value", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600, response.expiresInSeconds());
    }
}

