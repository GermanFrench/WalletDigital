package com.tuusuario.wallet.infrastructure.persistence;

import com.tuusuario.wallet.domain.model.AuthUser;
import com.tuusuario.wallet.domain.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA del repositorio de autenticación.
 */
@Repository
@RequiredArgsConstructor
public class AuthUserRepositoryAdapter implements AuthUserRepository {

    private final SpringDataAuthUserRepository authUserRepository;

    @Override
    public Optional<AuthUser> findByEmail(String email) {
        return authUserRepository.findByEmail(email.trim().toLowerCase()).map(this::toDomain);
    }

    @Override
    public Optional<AuthUser> findById(UUID id) {
        return authUserRepository.findById(id).map(this::toDomain);
    }

    @Override
    public AuthUser save(AuthUser authUser) {
        AuthUserJpaEntity entity = AuthUserJpaEntity.builder()
                .id(authUser.getId())
                .email(authUser.getEmail())
                .passwordHash(authUser.getPasswordHash())
                .role(authUser.getRole())
                .enabled(authUser.isEnabled())
                .build();

        return toDomain(authUserRepository.save(entity));
    }

    private AuthUser toDomain(AuthUserJpaEntity entity) {
        return AuthUser.create(
                entity.getId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getRole(),
                entity.isEnabled()
        );
    }
}

