package taylor.second_hand.product.moderation.platform.domain.model;

public record Price(
        String currency,
        Long amount,
        Integer exponent
) {}
