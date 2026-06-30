package taylor.second_hand.product.moderation.platform.domain.service;

import taylor.second_hand.product.moderation.platform.domain.enums.ProductState;
import taylor.second_hand.product.moderation.platform.domain.enums.UserRole;
import taylor.second_hand.product.moderation.platform.domain.exception.InvalidStateTransitionException;

import java.util.*;
import java.util.Optional;

import static taylor.second_hand.product.moderation.platform.domain.enums.ProductState.*;
import static taylor.second_hand.product.moderation.platform.domain.enums.UserRole.*;

/**
 * Pure domain service — no framework dependencies.
 * Encodes the complete state transition table and enforces it via {@link #validate}.
 */
public class ProductStateTransitionValidator {

    /**
     * Allowed transitions: from-state → ( to-state → set of roles permitted to make that move ).
     */
    private static final Map<ProductState, Map<ProductState, Set<UserRole>>> ALLOWED_TRANSITIONS =
            new EnumMap<>(ProductState.class);

    static {
        // Seller-driven transitions
        addTransition(DRAFT,         PENDING_REVIEW, SELLER);
        addTransition(REJECTED,      PENDING_REVIEW, SELLER);
        addTransition(DRAFT,         DELETED,        SELLER);
        addTransition(ACTIVE,        DELETED,        SELLER);
        addTransition(PAUSED,        DELETED,        SELLER);
        addTransition(REJECTED,      DELETED,        SELLER);

        // Moderator-driven transitions
        addTransition(PENDING_REVIEW, IN_REVIEW, MODERATOR);
        addTransition(IN_REVIEW,      ACTIVE,    MODERATOR);
        addTransition(IN_REVIEW,      PAUSED,    MODERATOR);
        addTransition(IN_REVIEW,      REJECTED,  MODERATOR);
    }

    private static void addTransition(ProductState from, ProductState to, UserRole... roles) {
        Set<UserRole> roleSet = EnumSet.noneOf(UserRole.class);
        roleSet.addAll(Arrays.asList(roles));
        ALLOWED_TRANSITIONS
                .computeIfAbsent(from, k -> new EnumMap<>(ProductState.class))
                .put(to, roleSet);
    }

    /**
     * Validates that {@code actorRole} may move a product from {@code current} to {@code target}.
     *
     * @throws InvalidStateTransitionException if the transition is unknown or the role is not permitted.
     */
    public void validate(ProductState current, ProductState target, UserRole actorRole) {
        Set<UserRole> permitted = Optional.ofNullable(ALLOWED_TRANSITIONS.get(current))
                .map(targets -> targets.get(target))
                .orElseThrow(() -> new InvalidStateTransitionException(current, target));

        Optional.of(permitted)
                .filter(roles -> roles.contains(actorRole))
                .orElseThrow(() -> new InvalidStateTransitionException(current, target, actorRole));
    }
}
