package at.fhv.productservice.infrastructure.persistence;


import at.fhv.productservice.application.mapper.ProductMapper;
import at.fhv.productservice.domain.model.Product;
import at.fhv.productservice.domain.model.ProductRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ProductRepositoryImpl implements ProductRepository {
    private final ProductJpaRepository jpaRepository;
    private final ProductMapper productMapper;

    public ProductRepositoryImpl(ProductJpaRepository jpaRepository, ProductMapper productMapper) {
        this.jpaRepository = jpaRepository;
        this.productMapper = productMapper;
    }

    @Override
    public Product save(Product product) {
        var entity = productMapper.toEntity(product);
        var saved = jpaRepository.save(entity);
        return productMapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return jpaRepository.findById(id).map(productMapper::toDomain);
    }

    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        for (var entity : jpaRepository.findAll()) {
            products.add(productMapper.toDomain(entity));
        }
        return products;
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
