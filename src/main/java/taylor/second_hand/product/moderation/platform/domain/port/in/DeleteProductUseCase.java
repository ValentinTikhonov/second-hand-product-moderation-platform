package taylor.second_hand.product.moderation.platform.domain.port.in;

public interface DeleteProductUseCase {

    void delete(String productId, Long sellerId);
}
