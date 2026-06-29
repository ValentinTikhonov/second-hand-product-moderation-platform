package taylor.second_hand.product.moderation.platform.domain.port.in;

import taylor.second_hand.product.moderation.platform.application.command.UpdateProductCommand;
import taylor.second_hand.product.moderation.platform.domain.model.Product;

public interface UpdateProductUseCase {

    Product update(UpdateProductCommand command);
}
