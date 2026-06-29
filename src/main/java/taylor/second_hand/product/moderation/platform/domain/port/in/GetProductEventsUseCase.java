package taylor.second_hand.product.moderation.platform.domain.port.in;

import taylor.second_hand.product.moderation.platform.domain.model.ProductEvent;

import java.util.List;

public interface GetProductEventsUseCase {

    List<ProductEvent> getEvents(String productId);
}
