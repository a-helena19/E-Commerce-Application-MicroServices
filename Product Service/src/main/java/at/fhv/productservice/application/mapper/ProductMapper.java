package at.fhv.productservice.application.mapper;

import at.fhv.productservice.domain.model.Product;
import at.fhv.productservice.infrastructure.persistence.ProductEntity;

public interface ProductMapper {
    Product toDomain(ProductEntity entity);
    ProductEntity toEntity(Product product);
}
