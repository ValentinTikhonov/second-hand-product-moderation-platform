package taylor.second_hand.product.moderation.platform.domain.model;

import java.util.List;

public record PagedResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
