package at.fhv.productservice.application.impl;


import at.fhv.productservice.application.mapper.ProductDTOMapper;
import at.fhv.productservice.application.services.UpdateProductService;
import at.fhv.productservice.domain.model.Product;
import at.fhv.productservice.domain.model.ProductRepository;
import at.fhv.productservice.domain.model.exception.ProductNotFoundException;
import at.fhv.productservice.rest.dtos.GetProductDTO;
import at.fhv.productservice.rest.dtos.UpdateProductDTO;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateProductServiceImpl implements UpdateProductService {
    private final ProductRepository productRepository;
    private final ProductDTOMapper productDTOMapper;

    public UpdateProductServiceImpl(ProductRepository productRepository, ProductDTOMapper productDTOMapper) {
        this.productRepository = productRepository;
        this.productDTOMapper = productDTOMapper;
    }

    @Override
    public GetProductDTO updateProduct(UUID id, UpdateProductDTO dto) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
        product.update(dto.getName(), dto.getDescription(), dto.getPrice(), dto.getStock());
        Product updatedProduct = productRepository.save(product);
        return productDTOMapper.toGetProductDTO(updatedProduct);
    }
}
