package taylor.second_hand.product.moderation.platform.infrastructure.web.dto.response;

public record UserDto(
        Long id,
        String username,
        String role
) {}
