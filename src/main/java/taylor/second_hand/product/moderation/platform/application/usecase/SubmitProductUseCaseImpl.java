package taylor.second_hand.product.moderation.platform.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import taylor.second_hand.product.moderation.platform.domain.enums.ProductEventType;
import taylor.second_hand.product.moderation.platform.domain.enums.ProductState;
import taylor.second_hand.product.moderation.platform.domain.enums.UserRole;
import taylor.second_hand.product.moderation.platform.domain.exception.ProductNotFoundException;
import taylor.second_hand.product.moderation.platform.domain.exception.UnauthorizedProductAccessException;
import taylor.second_hand.product.moderation.platform.domain.model.Product;
import taylor.second_hand.product.moderation.platform.domain.model.ProductEvent;
import taylor.second_hand.product.moderation.platform.domain.port.in.SubmitProductUseCase;
import taylor.second_hand.product.moderation.platform.domain.port.out.ProductEventPublisher;
import taylor.second_hand.product.moderation.platform.domain.port.out.ProductEventRepository;
import taylor.second_hand.product.moderation.platform.domain.port.out.ProductRepository;
import taylor.second_hand.product.moderation.platform.domain.service.ProductStateTransitionValidator;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubmitProductUseCaseImpl implements SubmitProductUseCase {

    private final ProductRepository productRepository;
    private final ProductEventRepository productEventRepository;
    private final ProductEventPublisher productEventPublisher;
    private final ProductStateTransitionValidator transitionValidator;

    @Override
    @Transactional
    public Product submit(String productId, Long sellerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (!product.getSellerId().equals(sellerId)) {
            throw new UnauthorizedProductAccessException(productId, sellerId);
        }

        transitionValidator.validate(product.getState(), ProductState.PENDING_REVIEW, UserRole.SELLER);

        Instant now = Instant.now();
        product.setState(ProductState.PENDING_REVIEW);
        product.setUpdatedAt(now);

        Product saved = productRepository.save(product);

        ProductEvent event = ProductEvent.builder()
                .id(UUID.randomUUID().toString())
                .productId(saved.getId())
                .eventType(ProductEventType.SUBMITTED)
                .actorId(sellerId)
                .timestamp(now)
                .build();

        productEventRepository.save(event);
        productEventPublisher.publish(event);

        return saved;
    }
}
