package taylor.second_hand.product.moderation.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import taylor.second_hand.product.moderation.platform.domain.enums.ProductEventType;

import java.time.Instant;

@Entity
@Table(name = "product_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEventEntity {

    /** UUID assigned by the application before persist. */
    @Id
    @Column(nullable = false, length = 50)
    private String id;

    @Column(name = "product_id", nullable = false, length = 50)
    private String productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private ProductEventType eventType;

    /** Null for system-generated events (e.g. CREATED by the platform itself). */
    @Column(name = "actor_id")
    private Long actorId;

    @Column(nullable = false)
    private Instant timestamp;

    /** JSON string — e.g. {"reason":"...", "prevState":"...", "newState":"..."}. */
    @Column(columnDefinition = "TEXT")
    private String metadata;
}
