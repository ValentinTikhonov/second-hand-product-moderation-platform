package taylor.second_hand.product.moderation.platform.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import taylor.second_hand.product.moderation.platform.domain.service.ProductStateTransitionValidator;

@Configuration
public class AppConfig {

    @Bean
    public ProductStateTransitionValidator productStateTransitionValidator() {
        return new ProductStateTransitionValidator();
    }
}
