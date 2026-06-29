package taylor.second_hand.product.moderation.platform.domain.port.out;

import taylor.second_hand.product.moderation.platform.domain.model.ProductEvent;

public interface ProductEventPublisher {

    void publish(ProductEvent event);
}
