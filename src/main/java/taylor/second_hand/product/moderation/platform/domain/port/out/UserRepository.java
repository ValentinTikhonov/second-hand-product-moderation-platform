package taylor.second_hand.product.moderation.platform.domain.port.out;

import taylor.second_hand.product.moderation.platform.domain.model.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    User save(User user);
}
