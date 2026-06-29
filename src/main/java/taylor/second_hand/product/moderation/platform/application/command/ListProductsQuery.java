package taylor.second_hand.product.moderation.platform.application.command;

import taylor.second_hand.product.moderation.platform.domain.enums.ProductCategory;
import taylor.second_hand.product.moderation.platform.domain.enums.ProductState;

public record ListProductsQuery(
        ProductState state,
        Long sellerId,
        ProductCategory category,
        int page,
        int size
) {}
