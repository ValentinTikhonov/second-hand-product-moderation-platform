package taylor.second_hand.product.moderation.platform.domain.port.out;

import taylor.second_hand.product.moderation.platform.domain.model.ProductEvent;

import java.util.List;

public interface ProductEventRepository {

    ProductEvent save(ProductEvent event);

    List<ProductEvent> findByProductIdOrderByTimestampAsc(String productId);
}
