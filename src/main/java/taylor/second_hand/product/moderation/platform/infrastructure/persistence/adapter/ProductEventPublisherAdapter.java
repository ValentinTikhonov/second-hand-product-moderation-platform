package taylor.second_hand.product.moderation.platform.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import taylor.second_hand.product.moderation.platform.domain.model.ProductEvent;
import taylor.second_hand.product.moderation.platform.domain.port.out.ProductEventPublisher;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventPublisherAdapter implements ProductEventPublisher {

    @Override
    public void publish(ProductEvent event) {
        log.info("ProductEvent published: type={} productId={} actorId={}",
                event.getEventType(), event.getProductId(), event.getActorId());
    }
}
