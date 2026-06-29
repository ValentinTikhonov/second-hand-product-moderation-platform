package taylor.second_hand.product.moderation.platform.domain.port.in;

import taylor.second_hand.product.moderation.platform.domain.model.Product;

public interface ApproveProductUseCase {

    Product approve(String productId, Long moderatorId);
}
