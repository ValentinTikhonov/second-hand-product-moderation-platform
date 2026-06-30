package taylor.second_hand.product.moderation.platform.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import taylor.second_hand.product.moderation.platform.domain.model.Product;
import taylor.second_hand.product.moderation.platform.infrastructure.persistence.entity.ProductEntity;

@Mapper(componentModel = "spring")
public interface ProductEntityMapper {

    /** version is managed by JPA — never set from domain. */
    @Mapping(target = "version", ignore = true)
    ProductEntity toEntity(Product product);

    Product toDomain(ProductEntity entity);
}
