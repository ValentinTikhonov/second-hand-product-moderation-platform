package taylor.second_hand.product.moderation.platform.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import taylor.second_hand.product.moderation.platform.domain.model.User;
import taylor.second_hand.product.moderation.platform.infrastructure.persistence.entity.UserEntity;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {

    UserEntity toEntity(User user);

    User toDomain(UserEntity entity);
}
