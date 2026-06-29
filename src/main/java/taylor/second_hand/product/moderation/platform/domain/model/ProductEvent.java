package taylor.second_hand.product.moderation.platform.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import taylor.second_hand.product.moderation.platform.domain.enums.ProductEventType;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEvent {

    private String id;
    private String productId;
    private ProductEventType eventType;
    /** Null for system-generated events. */
    private Long actorId;
    private Instant timestamp;
    /** Optional metadata e.g. rejection reason, previous/new state. */
    private Map<String, String> metadata;
}
