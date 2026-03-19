package com.tuusuario.wallet.application.service;

import com.tuusuario.wallet.application.usecase.AuthenticateUserCommand;
import com.tuusuario.wallet.application.usecase.AuthenticateUserUseCase;
import com.tuusuario.wallet.application.usecase.AuthenticationResult;
import com.tuusuario.wallet.application.usecase.LogoutUseCase;
import com.tuusuario.wallet.application.usecase.RefreshAccessTokenUseCase;
import com.tuusuario.wallet.application.usecase.RegisterUserCommand;
import com.tuusuario.wallet.application.usecase.RegisterUserUseCase;
import com.tuusuario.wallet.application.usecase.RequestPasswordResetUseCase;
import com.tuusuario.wallet.application.usecase.ResetPasswordUseCase;
import com.tuusuario.wallet.application.usecase.TokenProvider;
import com.tuusuario.wallet.domain.model.AuthUser;
import com.tuusuario.wallet.domain.model.PasswordResetToken;
import com.tuusuario.wallet.domain.model.RefreshToken;
import com.tuusuario.wallet.domain.repository.AuthUserRepository;
import com.tuusuario.wallet.domain.repository.PasswordResetTokenRepository;
import com.tuusuario.wallet.domain.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Servicio de aplicación para autenticación, refresh tokens y recuperación de contraseña.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthenticationApplicationService implements
        AuthenticateUserUseCase,
        RegisterUserUseCase,
        RefreshAccessTokenUseCase,
        LogoutUseCase,
        RequestPasswordResetUseCase,
        ResetPasswordUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationApplicationService.class);
    private static final int REFRESH_TOKEN_EXPIRY_DAYS = 30;
    private static final int RESET_TOKEN_EXPIRY_MINUTES = 30;

    private final AuthUserRepository authUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final Clock clock;
    private final PasswordResetNotificationService passwordResetNotificationService;

    @Value("${app.password-reset.expose-token-in-response:true}")
    private boolean exposeResetTokenInResponse;

    @Override
    @Transactional
    public AuthenticationResult authenticate(AuthenticateUserCommand command) {
        AuthUser authUser = authUserRepository.findByEmail(command.email())
                .filter(AuthUser::isEnabled)
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!passwordEncoder.matches(command.password(), authUser.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        return buildAuthResult(authUser);
    }

    @Override
    @Transactional
    public AuthenticationResult register(RegisterUserCommand command) {
        authUserRepository.findByEmail(command.email())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Ya existe un usuario registrado con ese email");
                });

        AuthUser authUser = authUserRepository.save(AuthUser.create(
                UUID.randomUUID(),
                command.email(),
                passwordEncoder.encode(command.password()),
                "USER",
                true
        ));

        return buildAuthResult(authUser);
    }

    @Override
    @Transactional
    public AuthenticationResult refresh(String rawRefreshToken) {
        String hash = sha256Hex(rawRefreshToken);
        Instant now = Instant.now(clock);

        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BadCredentialsException("Refresh token inválido"));

        if (!stored.isValid(now)) {
            throw new BadCredentialsException("Refresh token expirado o revocado");
        }

        // Revocar el token usado (rotación)
        refreshTokenRepository.save(stored.revoke());

        AuthUser authUser = authUserRepository.findById(stored.getAuthUserId())
                .filter(AuthUser::isEnabled)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado o deshabilitado"));

        return buildAuthResult(authUser);
    }

    @Override
    @Transactional
    public void logout(String rawRefreshToken) {
        String hash = sha256Hex(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(hash)
                .ifPresent(token -> refreshTokenRepository.revokeAllByAuthUserId(token.getAuthUserId()));
    }

    @Override
    @Transactional
    public String requestPasswordReset(String email) {
        // Si el email no existe, respondemos igual para no revelar si existe o no
        boolean userExists = authUserRepository.findByEmail(email).isPresent();
        if (!userExists) {
            log.info("Solicitud de reset para email no registrado: {}", email);
            return "";
        }

        String rawToken = generateSecureToken();
        String tokenHash = sha256Hex(rawToken);
        Instant now = Instant.now(clock);

        passwordResetTokenRepository.save(PasswordResetToken.create(
                UUID.randomUUID(),
                email,
                tokenHash,
                now.plus(RESET_TOKEN_EXPIRY_MINUTES, ChronoUnit.MINUTES),
                now
        ));

        boolean emailSent = passwordResetNotificationService.sendPasswordResetEmail(email, rawToken);
        if (!emailSent) {
            log.warn("No se pudo enviar email de recuperacion a {}", email);
        }

        if (exposeResetTokenInResponse) {
            log.info("Token de reset generado para {}: {}", email, rawToken);
            return rawToken;
        }

        log.info("Solicitud de reset registrada para {}", email);
        return "";
    }

    @Override
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        String hash = sha256Hex(rawToken);
        Instant now = Instant.now(clock);

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Token de recuperación inválido"));

        if (!resetToken.isValid(now)) {
            throw new IllegalArgumentException("El token de recuperación ha expirado o ya fue utilizado");
        }

        AuthUser authUser = authUserRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        AuthUser updated = AuthUser.create(
                authUser.getId(),
                authUser.getEmail(),
                passwordEncoder.encode(newPassword),
                authUser.getRole(),
                authUser.isEnabled()
        );
        authUserRepository.save(updated);

        passwordResetTokenRepository.save(resetToken.markAsUsed());

        // Revocar todos los refresh tokens al cambiar contraseña
        refreshTokenRepository.revokeAllByAuthUserId(authUser.getId());
    }

    // ── Helpers privados ─────────────────────────────────────────────────────

    @Transactional
    protected AuthenticationResult buildAuthResult(AuthUser authUser) {
        String rawRefreshToken = generateSecureToken();
        String refreshHash = sha256Hex(rawRefreshToken);
        Instant now = Instant.now(clock);

        refreshTokenRepository.save(RefreshToken.create(
                UUID.randomUUID(),
                authUser.getId(),
                refreshHash,
                now.plus(REFRESH_TOKEN_EXPIRY_DAYS, ChronoUnit.DAYS),
                now
        ));

        return new AuthenticationResult(
                tokenProvider.generateToken(authUser),
                "Bearer",
                tokenProvider.getExpirationInSeconds(),
                rawRefreshToken
        );
    }

    private static String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}

