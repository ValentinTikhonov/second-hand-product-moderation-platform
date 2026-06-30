package taylor.second_hand.product.moderation.platform.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import taylor.second_hand.product.moderation.platform.application.command.CreateProductCommand;
import taylor.second_hand.product.moderation.platform.domain.enums.ProductEventType;
import taylor.second_hand.product.moderation.platform.domain.enums.ProductState;
import taylor.second_hand.product.moderation.platform.domain.model.Product;
import taylor.second_hand.product.moderation.platform.domain.model.ProductEvent;
import taylor.second_hand.product.moderation.platform.domain.port.in.CreateProductUseCase;
import taylor.second_hand.product.moderation.platform.domain.port.out.ProductEventPublisher;
import taylor.second_hand.product.moderation.platform.domain.port.out.ProductEventRepository;
import taylor.second_hand.product.moderation.platform.domain.port.out.ProductRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateProductUseCaseImpl implements CreateProductUseCase {

    private final ProductRepository productRepository;
    private final ProductEventRepository productEventRepository;
    private final ProductEventPublisher productEventPublisher;

    @Override
    @Transactional
    public Product create(CreateProductCommand command) {
        Instant now = Instant.now();

        Product product = Product.builder()
                .id(UUID.randomUUID().toString())
                .sellerId(command.sellerId())
                .title(command.title())
                .description(command.description())
                .category(command.category())
                .size(command.size())
                .condition(command.condition())
                .price(command.price())
                .imageUrls(command.imageUrls())
                .termsAccepted(command.termsAccepted())
                .state(ProductState.DRAFT)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Product saved = productRepository.save(product);

        ProductEvent event = ProductEvent.builder()
                .id(UUID.randomUUID().toString())
                .productId(saved.getId())
                .eventType(ProductEventType.CREATED)
                .actorId(command.sellerId())
                .timestamp(now)
                .build();

        productEventRepository.save(event);
        productEventPublisher.publish(event);

        return saved;
    }
}
