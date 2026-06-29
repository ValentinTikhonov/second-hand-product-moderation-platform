package taylor.second_hand.product.moderation.platform.application.command;

public record RejectProductCommand(
        String productId,
        Long moderatorId,
        String reason
) {}
