package taylor.second_hand.product.moderation.platform.domain.port.in;

import taylor.second_hand.product.moderation.platform.application.command.ListProductsQuery;
import taylor.second_hand.product.moderation.platform.domain.model.PagedResult;
import taylor.second_hand.product.moderation.platform.domain.model.Product;

public interface ListProductsUseCase {

    PagedResult<Product> list(ListProductsQuery query);
}
