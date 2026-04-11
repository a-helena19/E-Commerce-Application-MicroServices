package at.fhv.productservice.application.impl;


import at.fhv.productservice.application.mapper.ProductDTOMapper;
import at.fhv.productservice.application.metrics.ProductMetricsService;
import at.fhv.productservice.application.services.UpdateProductService;
import at.fhv.productservice.domain.model.Product;
import at.fhv.productservice.domain.model.ProductRepository;
import at.fhv.productservice.domain.model.ProductStatus;
import at.fhv.productservice.domain.model.exception.ProductNotFoundException;
import at.fhv.productservice.rest.dtos.GetProductDTO;
import at.fhv.productservice.rest.dtos.UpdateProductDTO;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateProductServiceImpl implements UpdateProductService {
    private final ProductRepository productRepository;
    private final ProductDTOMapper productDTOMapper;
    private final ProductMetricsService productMetricsService;

    public UpdateProductServiceImpl(ProductRepository productRepository, ProductDTOMapper productDTOMapper, ProductMetricsService productMetricsService) {
        this.productRepository = productRepository;
        this.productDTOMapper = productDTOMapper;
        this.productMetricsService = productMetricsService;
    }

    @Override
    public GetProductDTO updateProduct(UUID id, UpdateProductDTO dto) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
        product.update(dto.getName(), dto.getDescription(), dto.getPrice(), dto.getStock());
        Product updatedProduct = productRepository.save(product);
        int totalStock = productRepository.findAll().stream().filter(p -> p.getStatus() == ProductStatus.ACTIVE).mapToInt(Product::getStock).sum();
        productMetricsService.updateStockLevel(totalStock);
        return productDTOMapper.toGetProductDTO(updatedProduct);
    }

    @Override
    public GetProductDTO reduceStock(UUID productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock for product " + productId + ". Available: " + product.getStock() + ", Requested: " + quantity);
        }

        product.reduceStock(quantity);
        Product updatedProduct = productRepository.save(product);
        int totalStock = productRepository.findAll().stream().filter(p -> p.getStatus() == ProductStatus.ACTIVE).mapToInt(Product::getStock).sum();
        productMetricsService.updateStockLevel(totalStock);
        return productDTOMapper.toGetProductDTO(updatedProduct);
    }

    @Override
    public GetProductDTO restoreStock(UUID productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.increaseStock(quantity);
        Product updatedProduct = productRepository.save(product);
        int totalStock = productRepository.findAll().stream().filter(p -> p.getStatus() == ProductStatus.ACTIVE).mapToInt(Product::getStock).sum();
        productMetricsService.updateStockLevel(totalStock);
        return productDTOMapper.toGetProductDTO(updatedProduct);
    }
}
