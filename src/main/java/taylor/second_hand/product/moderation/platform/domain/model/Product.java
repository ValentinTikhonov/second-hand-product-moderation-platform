package taylor.second_hand.product.moderation.platform.domain.model;

import lombok.*;
import taylor.second_hand.product.moderation.platform.domain.enums.ProductCategory;
import taylor.second_hand.product.moderation.platform.domain.enums.ProductCondition;
import taylor.second_hand.product.moderation.platform.domain.enums.ProductSize;
import taylor.second_hand.product.moderation.platform.domain.enums.ProductState;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
public class Product {

    private String id;
    private Long sellerId;
    private String title;
    private String description;
    private ProductCategory category;
    private ProductSize size;
    private ProductCondition condition;
    private ProductState state;
    private Price price;
    private List<String> imageUrls;
    /** Null until a moderator claims the product (IN_REVIEW). */
    private Long reviewerId;
    private boolean termsAccepted;
    private Instant createdAt;
    private Instant updatedAt;
}
