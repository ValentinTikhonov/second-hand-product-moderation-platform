package taylor.second_hand.product.moderation.platform.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import taylor.second_hand.product.moderation.platform.domain.model.ProductEvent;
import taylor.second_hand.product.moderation.platform.infrastructure.persistence.entity.ProductEventEntity;

@Mapper(componentModel = "spring")
public interface EventEntityMapper {

    /**
     * metadata (Map → JSON String) is handled manually in the adapter
     * to avoid coupling the mapper to Jackson.
     */
    @Mapping(target = "metadata", ignore = true)
    ProductEventEntity toEntity(ProductEvent event);

    @Mapping(target = "metadata", ignore = true)
    ProductEvent toDomain(ProductEventEntity entity);
}
