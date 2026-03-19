package com.tuusuario.wallet.infrastructure.persistence;

import com.tuusuario.wallet.domain.model.PasswordResetToken;
import com.tuusuario.wallet.domain.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adaptador JPA para tokens de recuperación de contraseña.
 */
@Repository
@RequiredArgsConstructor
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepository {

    private final SpringDataPasswordResetTokenRepository springDataRepository;

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        PasswordResetTokenJpaEntity entity = springDataRepository.findById(token.getId())
                .map(existing -> {
                    existing.setUsed(token.isUsed());
                    return existing;
                })
                .orElseGet(() -> PasswordResetTokenJpaEntity.builder()
                        .id(token.getId())
                        .email(token.getEmail())
                        .tokenHash(token.getTokenHash())
                        .expiresAt(token.getExpiresAt())
                        .used(token.isUsed())
                        .createdAt(token.getCreatedAt())
                        .build());

        return toDomain(springDataRepository.save(entity));
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return springDataRepository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    private PasswordResetToken toDomain(PasswordResetTokenJpaEntity entity) {
        return PasswordResetToken.restore(
                entity.getId(),
                entity.getEmail(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.isUsed(),
                entity.getCreatedAt()
        );
    }
}
