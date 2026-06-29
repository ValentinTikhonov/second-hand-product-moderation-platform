package taylor.second_hand.product.moderation.platform.domain.exception;

import taylor.second_hand.product.moderation.platform.domain.enums.ProductState;
import taylor.second_hand.product.moderation.platform.domain.enums.UserRole;

public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(ProductState from, ProductState to) {
        super(String.format("Transition from %s to %s is not allowed.", from, to));
    }

    public InvalidStateTransitionException(ProductState from, ProductState to, UserRole role) {
        super(String.format("Role %s is not permitted to transition a product from %s to %s.", role, from, to));
    }
}
