package taylor.second_hand.product.moderation.platform.domain.port.out;

import taylor.second_hand.product.moderation.platform.application.command.ListProductsQuery;
import taylor.second_hand.product.moderation.platform.domain.enums.ProductState;
import taylor.second_hand.product.moderation.platform.domain.model.PagedResult;
import taylor.second_hand.product.moderation.platform.domain.model.Product;

import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(String id);

    PagedResult<Product> findAll(ListProductsQuery query);

    /** Returns the oldest product in the given state (used for claim-next). */
    Optional<Product> findOldestByState(ProductState state);
}
