package at.fhv.productservice.application.impl;


import at.fhv.productservice.application.mapper.ProductDTOMapper;
import at.fhv.productservice.application.metrics.ProductMetricsService;
import at.fhv.productservice.application.services.GetProductService;
import at.fhv.productservice.domain.model.Product;
import at.fhv.productservice.domain.model.ProductRepository;
import at.fhv.productservice.domain.model.ProductStatus;
import at.fhv.productservice.domain.model.exception.ProductNotFoundException;
import at.fhv.productservice.rest.dtos.GetProductDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GetProductServiceImpl implements GetProductService {
    private final ProductRepository productRepository;
    private final ProductDTOMapper productDTOMapper;
    private final ProductMetricsService productMetricsService;

    public GetProductServiceImpl(ProductRepository productRepository, ProductDTOMapper productDTOMapper, ProductMetricsService productMetricsService) {
        this.productRepository = productRepository;
        this.productDTOMapper = productDTOMapper;
        this.productMetricsService = productMetricsService;
    }

    @Override
    public GetProductDTO getProductById(UUID id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
        productMetricsService.incrementProductsViewed();
        return productDTOMapper.toGetProductDTO(product);
    }

    @Override
    public List<GetProductDTO> getAllProducts() {
        List<Product> allProducts = productRepository.findAll();
        productMetricsService.incrementProductsViewed();
        List<GetProductDTO> activeProducts = new ArrayList<>();

        for (Product product : allProducts) {
            if (product.getStatus() == ProductStatus.ACTIVE) {
                activeProducts.add(productDTOMapper.toGetProductDTO(product));
            }
        }

        return activeProducts;
    }

    @Override
    public List<GetProductDTO> getAllProducts(boolean includeInactive) {
        List<Product> allProducts = productRepository.findAll();
        List<GetProductDTO> result = new ArrayList<>();

        for (Product product : allProducts) {
            if (includeInactive || product.getStatus() == ProductStatus.ACTIVE) {
                result.add(productDTOMapper.toGetProductDTO(product));
            }
        }

        return result;
    }
}
