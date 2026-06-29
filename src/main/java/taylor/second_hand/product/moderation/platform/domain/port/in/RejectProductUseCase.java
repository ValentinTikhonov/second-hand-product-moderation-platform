package taylor.second_hand.product.moderation.platform.domain.port.in;

import taylor.second_hand.product.moderation.platform.application.command.RejectProductCommand;
import taylor.second_hand.product.moderation.platform.domain.model.Product;

public interface RejectProductUseCase {

    Product reject(RejectProductCommand command);
}
