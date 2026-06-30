package taylor.second_hand.product.moderation.platform.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {}
