package com.tuusuario.wallet.infrastructure.persistence;

import com.tuusuario.wallet.domain.model.User;
import com.tuusuario.wallet.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia para usuarios.
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository userRepository;

    @Override
    public User save(User user) {
        UserJpaEntity entity = UserJpaEntity.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();

        return toDomain(userRepository.save(entity));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase()).map(this::toDomain);
    }

    private User toDomain(UserJpaEntity entity) {
        return User.create(entity.getId(), entity.getName(), entity.getEmail());
    }
}

