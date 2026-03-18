package com.tuusuario.wallet.infrastructure.security;

import com.tuusuario.wallet.application.usecase.TokenProvider;
import com.tuusuario.wallet.domain.model.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Implementación JWT del proveedor de tokens.
 */
@Component
public class JwtTokenProvider implements TokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateToken(AuthUser authUser) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(getExpirationInSeconds());

        return Jwts.builder()
                .subject(authUser.getEmail())
                .claim("role", authUser.getRole())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    @Override
    public String extractSubject(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }

    @Override
    public long getExpirationInSeconds() {
        return jwtProperties.expirationMinutes() * 60;
    }
}

