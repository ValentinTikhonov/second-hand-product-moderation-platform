package taylor.second_hand.product.moderation.platform.application.command;

import taylor.second_hand.product.moderation.platform.domain.enums.ProductCategory;
import taylor.second_hand.product.moderation.platform.domain.enums.ProductCondition;
import taylor.second_hand.product.moderation.platform.domain.enums.ProductSize;
import taylor.second_hand.product.moderation.platform.domain.model.Price;

import java.util.List;

public record UpdateProductCommand(
        String productId,
        Long sellerId,
        String title,
        String description,
        ProductCategory category,
        ProductSize size,
        ProductCondition condition,
        Price price,
        List<String> imageUrls
) {}
