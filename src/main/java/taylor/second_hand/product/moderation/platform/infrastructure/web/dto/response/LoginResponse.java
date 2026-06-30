package taylor.second_hand.product.moderation.platform.infrastructure.web.dto.response;

public record LoginResponse(
        String accessToken,
        UserDto user
) {}
