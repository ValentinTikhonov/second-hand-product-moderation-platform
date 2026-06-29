package taylor.second_hand.product.moderation.platform.domain.port.in;

import taylor.second_hand.product.moderation.platform.application.command.CreateProductCommand;
import taylor.second_hand.product.moderation.platform.domain.model.Product;

public interface CreateProductUseCase {

    Product create(CreateProductCommand command);
}
