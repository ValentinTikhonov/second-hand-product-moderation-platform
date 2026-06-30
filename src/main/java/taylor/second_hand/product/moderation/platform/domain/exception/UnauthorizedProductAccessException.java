package taylor.second_hand.product.moderation.platform.domain.exception;

public class UnauthorizedProductAccessException extends RuntimeException {

    public UnauthorizedProductAccessException(String productId, Long userId) {
        super("User " + userId + " is not the owner of product " + productId);
    }
}
