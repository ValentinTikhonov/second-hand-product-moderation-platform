package taylor.second_hand.product.moderation.platform.infrastructure.persistence.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import taylor.second_hand.product.moderation.platform.domain.model.ProductEvent;
import taylor.second_hand.product.moderation.platform.domain.port.out.ProductEventRepository;
import taylor.second_hand.product.moderation.platform.infrastructure.persistence.entity.ProductEventEntity;
import taylor.second_hand.product.moderation.platform.infrastructure.persistence.jpa.JpaProductEventRepository;
import taylor.second_hand.product.moderation.platform.infrastructure.persistence.mapper.EventEntityMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventRepositoryAdapter implements ProductEventRepository {

    private final JpaProductEventRepository jpaProductEventRepository;
    private final EventEntityMapper mapper;
    private final ObjectMapper objectMapper;

    @Override
    public ProductEvent save(ProductEvent event) {
        ProductEventEntity entity = mapper.toEntity(event);
        entity.setMetadata(toJson(event.getMetadata()));
        ProductEventEntity saved = jpaProductEventRepository.save(entity);
        ProductEvent result = mapper.toDomain(saved);
        result.setMetadata(fromJson(saved.getMetadata()));
        return result;
    }

    @Override
    public List<ProductEvent> findByProductIdOrderByTimestampAsc(String productId) {
        return jpaProductEventRepository.findByProductIdOrderByTimestampAsc(productId)
                .stream()
                .map(entity -> {
                    ProductEvent event = mapper.toDomain(entity);
                    event.setMetadata(fromJson(entity.getMetadata()));
                    return event;
                })
                .toList();
    }

    private String toJson(Map<String, String> metadata) {
        return Optional.ofNullable(metadata)
                .filter(m -> !m.isEmpty())
                .map(m -> {
                    try {
                        return objectMapper.writeValueAsString(m);
                    } catch (JsonProcessingException e) {
                        log.warn("Failed to serialize event metadata", e);
                        return null;
                    }
                })
                .orElse(null);
    }

    private Map<String, String> fromJson(String json) {
        return Optional.ofNullable(json)
                .filter(s -> !s.isBlank())
                .map(s -> {
                    try {
                        return objectMapper.readValue(s, new TypeReference<Map<String, String>>() {});
                    } catch (JsonProcessingException e) {
                        log.warn("Failed to deserialize event metadata: {}", s, e);
                        return null;
                    }
                })
                .orElse(null);
    }
}
