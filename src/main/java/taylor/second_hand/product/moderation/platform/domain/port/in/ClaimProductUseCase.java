package taylor.second_hand.product.moderation.platform.domain.port.in;

import taylor.second_hand.product.moderation.platform.domain.model.Product;

public interface ClaimProductUseCase {

    Product claim(Long moderatorId);
}
