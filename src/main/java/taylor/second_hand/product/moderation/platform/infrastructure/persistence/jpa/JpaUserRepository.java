package taylor.second_hand.product.moderation.platform.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import taylor.second_hand.product.moderation.platform.infrastructure.persistence.entity.UserEntity;

import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);
}
