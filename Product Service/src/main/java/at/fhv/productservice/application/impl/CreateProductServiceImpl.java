package at.fhv.productservice.application.impl;


import at.fhv.productservice.application.mapper.ProductDTOMapper;
import at.fhv.productservice.application.metrics.ProductMetricsService;
import at.fhv.productservice.application.services.CreateProductService;
import at.fhv.productservice.domain.model.Product;
import at.fhv.productservice.domain.model.ProductRepository;
import at.fhv.productservice.domain.model.ProductStatus;
import at.fhv.productservice.rest.dtos.CreateProductDTO;
import at.fhv.productservice.rest.dtos.GetProductDTO;
import org.springframework.stereotype.Service;

@Service
public class CreateProductServiceImpl implements CreateProductService {
    private final ProductRepository productRepository;
    private final ProductDTOMapper productDTOMapper;
    private final ProductMetricsService productMetricsService;

    public CreateProductServiceImpl(ProductRepository productRepository, ProductDTOMapper productDTOMapper, ProductMetricsService productMetricsService) {
        this.productRepository = productRepository;
        this.productDTOMapper = productDTOMapper;
        this.productMetricsService = productMetricsService;
    }

    @Override
    public GetProductDTO createProduct(CreateProductDTO dto) {
        Product product = productDTOMapper.toDomainCreateProductDTO(dto);
        Product savedProduct = productRepository.save(product);
        productMetricsService.incrementProductsAdded();
        int totalStock = productRepository.findAll().stream().filter(p -> p.getStatus() == ProductStatus.ACTIVE).mapToInt(Product::getStock).sum();
        productMetricsService.updateStockLevel(totalStock);
        return productDTOMapper.toGetProductDTO(savedProduct);
    }
}
