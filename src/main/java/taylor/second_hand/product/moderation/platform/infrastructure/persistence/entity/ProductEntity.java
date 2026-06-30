package taylor.second_hand.product.moderation.platform.infrastructure.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import taylor.second_hand.product.moderation.platform.domain.enums.ProductCategory;
import taylor.second_hand.product.moderation.platform.domain.enums.ProductCondition;
import taylor.second_hand.product.moderation.platform.domain.enums.ProductSize;
import taylor.second_hand.product.moderation.platform.domain.enums.ProductState;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {

    /** UUID assigned by the application before persist — no auto-generation. */
    @Id
    @Column(nullable = false, length = 50)
    private String id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductSize size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductCondition condition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductState state;

    @Embedded
    private PriceEmbeddable price;

    @ElementCollection
    @CollectionTable(
            name = "product_image_urls",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @Column(name = "url", nullable = false, length = 500)
    @OrderColumn(name = "sort_order")
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "terms_accepted", nullable = false)
    private boolean termsAccepted;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Optimistic locking — prevents lost updates on concurrent moderator actions. */
    @Version
    private Long version;
}
