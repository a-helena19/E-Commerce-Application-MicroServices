package at.fhv.productservice.infrastructure.mapper;


import at.fhv.productservice.application.mapper.ProductMapper;
import at.fhv.productservice.domain.model.Product;
import at.fhv.productservice.domain.model.ProductStatus;
import at.fhv.productservice.infrastructure.persistence.ProductEntity;
import at.fhv.productservice.infrastructure.persistence.ProdStatus;
import org.springframework.stereotype.Component;

@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public Product toDomain(ProductEntity entity) {
        if (entity == null) {
            return null;
        }
        return Product.reconstruct(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getStock(),
                ProductStatus.valueOf(entity.getStatus().name())
        );
    }

    @Override
    public ProductEntity toEntity(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductEntity(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                ProdStatus.valueOf(product.getStatus().name())
        );
    }

}
