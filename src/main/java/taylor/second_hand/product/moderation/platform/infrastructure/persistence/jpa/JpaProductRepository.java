package taylor.second_hand.product.moderation.platform.infrastructure.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import taylor.second_hand.product.moderation.platform.domain.enums.ProductCategory;
import taylor.second_hand.product.moderation.platform.domain.enums.ProductState;
import taylor.second_hand.product.moderation.platform.infrastructure.persistence.entity.ProductEntity;

import java.util.Optional;

public interface JpaProductRepository extends JpaRepository<ProductEntity, String> {

    /**
     * Filtered listing — all three parameters are optional (null = ignore that filter).
     * Used by {@code ProductRepositoryAdapter.findAll(ListProductsQuery)}.
     */
    @Query("""
            SELECT p FROM ProductEntity p
            WHERE (:state    IS NULL OR p.state    = :state)
              AND (:sellerId  IS NULL OR p.sellerId = :sellerId)
              AND (:category  IS NULL OR p.category = :category)
            ORDER BY p.createdAt DESC
            """)
    Page<ProductEntity> findByOptionalFilters(
            @Param("state")    ProductState state,
            @Param("sellerId") Long sellerId,
            @Param("category") ProductCategory category,
            Pageable pageable
    );

    /** Returns the oldest PENDING_REVIEW product for claim-next logic. */
    Optional<ProductEntity> findFirstByStateOrderByCreatedAtAsc(ProductState state);
}
