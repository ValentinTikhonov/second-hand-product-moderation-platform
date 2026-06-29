package taylor.second_hand.product.moderation.platform.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import taylor.second_hand.product.moderation.platform.domain.enums.UserRole;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String username;
    private String displayName;
    private String passwordHash;
    private UserRole role;
    private boolean blocked;
}
