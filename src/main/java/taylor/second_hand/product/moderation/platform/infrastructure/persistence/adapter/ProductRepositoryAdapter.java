package taylor.second_hand.product.moderation.platform.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import taylor.second_hand.product.moderation.platform.application.command.ListProductsQuery;
import taylor.second_hand.product.moderation.platform.domain.enums.ProductState;
import taylor.second_hand.product.moderation.platform.domain.model.PagedResult;
import taylor.second_hand.product.moderation.platform.domain.model.Product;
import taylor.second_hand.product.moderation.platform.domain.port.out.ProductRepository;
import taylor.second_hand.product.moderation.platform.infrastructure.persistence.entity.ProductEntity;
import taylor.second_hand.product.moderation.platform.infrastructure.persistence.jpa.JpaProductRepository;
import taylor.second_hand.product.moderation.platform.infrastructure.persistence.mapper.ProductEntityMapper;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    private final JpaProductRepository jpaProductRepository;
    private final ProductEntityMapper mapper;

    @Override
    public Product save(Product product) {
        return mapper.toDomain(jpaProductRepository.save(mapper.toEntity(product)));
    }

    @Override
    public Optional<Product> findById(String id) {
        return jpaProductRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public PagedResult<Product> findAll(ListProductsQuery query) {
        Page<ProductEntity> page = jpaProductRepository.findByOptionalFilters(
                query.state(),
                query.sellerId(),
                query.category(),
                PageRequest.of(query.page(), query.size())
        );
        return new PagedResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    public Optional<Product> findOldestByState(ProductState state) {
        return jpaProductRepository.findFirstByStateOrderByCreatedAtAsc(state).map(mapper::toDomain);
    }
}
