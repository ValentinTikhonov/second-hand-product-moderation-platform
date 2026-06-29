package taylor.second_hand.product.moderation.platform.domain.model;

import lombok.*;
import taylor.second_hand.product.moderation.platform.domain.enums.UserRole;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
public class User {

    private Long id;
    private String username;
    private String displayName;
    private String passwordHash;
    private UserRole role;
    private boolean blocked;
}
