package taylor.second_hand.product.moderation.platform.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import taylor.second_hand.product.moderation.platform.domain.model.User;
import taylor.second_hand.product.moderation.platform.domain.port.out.UserRepository;
import taylor.second_hand.product.moderation.platform.infrastructure.persistence.jpa.JpaUserRepository;
import taylor.second_hand.product.moderation.platform.infrastructure.persistence.mapper.UserEntityMapper;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;
    private final UserEntityMapper mapper;

    @Override
    public Optional<User> findById(Long id) {
        return jpaUserRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaUserRepository.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public User save(User user) {
        return mapper.toDomain(jpaUserRepository.save(mapper.toEntity(user)));
    }
}
