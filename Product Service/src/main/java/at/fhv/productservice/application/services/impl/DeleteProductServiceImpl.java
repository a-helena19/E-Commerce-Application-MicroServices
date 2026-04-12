package at.fhv.productservice.application.services.impl;


import at.fhv.productservice.application.mapper.ProductDTOMapper;
import at.fhv.productservice.application.metrics.ProductMetricsService;
import at.fhv.productservice.application.services.DeleteProductService;
import at.fhv.productservice.domain.model.Product;
import at.fhv.productservice.domain.model.ProductRepository;
import at.fhv.productservice.domain.model.ProductStatus;
import at.fhv.productservice.domain.model.exception.ProductNotFoundException;
import at.fhv.productservice.rest.dtos.GetProductDTO;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteProductServiceImpl implements DeleteProductService {
    private final ProductRepository productRepository;
    private final ProductDTOMapper productDTOMapper;
    private final ProductMetricsService productMetricsService;

    public DeleteProductServiceImpl(ProductRepository productRepository, ProductDTOMapper productDTOMapper, ProductMetricsService productMetricsService) {
        this.productRepository = productRepository;
        this.productDTOMapper = productDTOMapper;
        this.productMetricsService = productMetricsService;
    }

    @Override
    public GetProductDTO deleteProduct(UUID id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
        product.delete();
        Product deletedProduct = productRepository.save(product);
        int totalStock = productRepository.findAll().stream()
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .mapToInt(Product::getStock)
                .sum();
        productMetricsService.updateStockLevel(totalStock);
        return productDTOMapper.toGetProductDTO(deletedProduct);
    }

}
