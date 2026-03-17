package at.fhv.productservice.application.impl;


import at.fhv.productservice.application.mapper.ProductDTOMapper;
import at.fhv.productservice.application.services.DeleteProductService;
import at.fhv.productservice.domain.model.Product;
import at.fhv.productservice.domain.model.ProductRepository;
import at.fhv.productservice.domain.model.exception.ProductNotFoundException;
import at.fhv.productservice.rest.dtos.GetProductDTO;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteProductServiceImpl implements DeleteProductService {
    private final ProductRepository productRepository;
    private final ProductDTOMapper productDTOMapper;

    public DeleteProductServiceImpl(ProductRepository productRepository, ProductDTOMapper productDTOMapper) {
        this.productRepository = productRepository;
        this.productDTOMapper = productDTOMapper;
    }

    @Override
    public GetProductDTO deleteProduct(UUID id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
        product.delete();
        Product deletedProduct = productRepository.save(product);
        return productDTOMapper.toGetProductDTO(deletedProduct);
    }

}
