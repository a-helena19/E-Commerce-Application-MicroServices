package at.fhv.productservice.application.impl;


import at.fhv.productservice.application.mapper.ProductDTOMapper;
import at.fhv.productservice.application.services.CreateProductService;
import at.fhv.productservice.domain.model.Product;
import at.fhv.productservice.domain.model.ProductRepository;
import at.fhv.productservice.rest.dtos.CreateProductDTO;
import at.fhv.productservice.rest.dtos.GetProductDTO;
import org.springframework.stereotype.Service;

@Service
public class CreateProductServiceImpl implements CreateProductService {
    private final ProductRepository productRepository;
    private final ProductDTOMapper productDTOMapper;

    public CreateProductServiceImpl(ProductRepository productRepository, ProductDTOMapper productDTOMapper) {
        this.productRepository = productRepository;
        this.productDTOMapper = productDTOMapper;
    }

    @Override
    public GetProductDTO createProduct(CreateProductDTO dto) {
        Product product = productDTOMapper.toDomainCreateProductDTO(dto);
        Product savedProduct = productRepository.save(product);
        return productDTOMapper.toGetProductDTO(savedProduct);
    }
}
