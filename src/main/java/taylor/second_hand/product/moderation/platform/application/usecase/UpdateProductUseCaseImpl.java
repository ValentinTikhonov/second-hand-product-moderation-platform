package taylor.second_hand.product.moderation.platform.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import taylor.second_hand.product.moderation.platform.application.command.UpdateProductCommand;
import taylor.second_hand.product.moderation.platform.domain.enums.ProductEventType;
import taylor.second_hand.product.moderation.platform.domain.enums.ProductState;
import taylor.second_hand.product.moderation.platform.domain.exception.InvalidStateTransitionException;
import taylor.second_hand.product.moderation.platform.domain.exception.ProductNotFoundException;
import taylor.second_hand.product.moderation.platform.domain.exception.UnauthorizedProductAccessException;
import taylor.second_hand.product.moderation.platform.domain.model.Product;
import taylor.second_hand.product.moderation.platform.domain.model.ProductEvent;
import taylor.second_hand.product.moderation.platform.domain.port.in.UpdateProductUseCase;
import taylor.second_hand.product.moderation.platform.domain.port.out.ProductEventPublisher;
import taylor.second_hand.product.moderation.platform.domain.port.out.ProductEventRepository;
import taylor.second_hand.product.moderation.platform.domain.port.out.ProductRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateProductUseCaseImpl implements UpdateProductUseCase {

    /** Only products in these states may be edited by the seller. */
    private static final Set<ProductState> EDITABLE_STATES = Set.of(
            ProductState.DRAFT,
            ProductState.REJECTED
    );

    private final ProductRepository productRepository;
    private final ProductEventRepository productEventRepository;
    private final ProductEventPublisher productEventPublisher;

    @Override
    @Transactional
    public Product update(UpdateProductCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        if (!product.getSellerId().equals(command.sellerId())) {
            throw new UnauthorizedProductAccessException(command.productId(), command.sellerId());
        }

        if (!EDITABLE_STATES.contains(product.getState())) {
            throw new InvalidStateTransitionException(product.getState(), product.getState());
        }

        applyPartialUpdate(product, command);
        product.setUpdatedAt(Instant.now());

        Product saved = productRepository.save(product);

        ProductEvent event = ProductEvent.builder()
                .id(UUID.randomUUID().toString())
                .productId(saved.getId())
                .eventType(ProductEventType.UPDATED)
                .actorId(command.sellerId())
                .timestamp(saved.getUpdatedAt())
                .build();

        productEventRepository.save(event);
        productEventPublisher.publish(event);

        return saved;
    }

    private void applyPartialUpdate(Product product, UpdateProductCommand command) {
        Optional.ofNullable(command.title())      .ifPresent(product::setTitle);
        Optional.ofNullable(command.description()).ifPresent(product::setDescription);
        Optional.ofNullable(command.category())   .ifPresent(product::setCategory);
        Optional.ofNullable(command.size())       .ifPresent(product::setSize);
        Optional.ofNullable(command.condition())  .ifPresent(product::setCondition);
        Optional.ofNullable(command.price())      .ifPresent(product::setPrice);
        Optional.ofNullable(command.imageUrls())  .ifPresent(product::setImageUrls);
    }
}
