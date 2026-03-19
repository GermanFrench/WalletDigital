package com.tuusuario.wallet.infrastructure.persistence;

import com.tuusuario.wallet.domain.model.RefreshToken;
import com.tuusuario.wallet.domain.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA para refresh tokens.
 */
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository springDataRepository;

    @Override
    public RefreshToken save(RefreshToken token) {
        RefreshTokenJpaEntity entity = springDataRepository.findById(token.getId())
                .map(existing -> {
                    existing.setRevoked(token.isRevoked());
                    return existing;
                })
                .orElseGet(() -> RefreshTokenJpaEntity.builder()
                        .id(token.getId())
                        .authUserId(token.getAuthUserId())
                        .tokenHash(token.getTokenHash())
                        .expiresAt(token.getExpiresAt())
                        .revoked(token.isRevoked())
                        .createdAt(token.getCreatedAt())
                        .build());

        return toDomain(springDataRepository.save(entity));
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return springDataRepository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    @Transactional
    public void revokeAllByAuthUserId(UUID authUserId) {
        springDataRepository.revokeAllByAuthUserId(authUserId);
    }

    private RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return RefreshToken.restore(
                entity.getId(),
                entity.getAuthUserId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.isRevoked(),
                entity.getCreatedAt()
        );
    }
}
