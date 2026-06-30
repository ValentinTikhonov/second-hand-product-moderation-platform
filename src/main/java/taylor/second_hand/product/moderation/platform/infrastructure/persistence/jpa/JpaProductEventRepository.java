package taylor.second_hand.product.moderation.platform.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import taylor.second_hand.product.moderation.platform.infrastructure.persistence.entity.ProductEventEntity;

import java.util.List;

public interface JpaProductEventRepository extends JpaRepository<ProductEventEntity, String> {

    List<ProductEventEntity> findByProductIdOrderByTimestampAsc(String productId);
}
