package taylor.second_hand.product.moderation.platform.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceEmbeddable {

    @Column(name = "price_currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "price_amount", nullable = false)
    private Long amount;

    @Column(name = "price_exponent", nullable = false)
    private Integer exponent;
}
