package at.fhv.productservice.application.mapper;

import at.fhv.productservice.domain.model.Product;
import at.fhv.productservice.rest.dtos.CreateProductDTO;
import at.fhv.productservice.rest.dtos.GetProductDTO;

public interface ProductDTOMapper {
    GetProductDTO toGetProductDTO(Product product);
    Product toDomainCreateProductDTO(CreateProductDTO createProductDTO);
}
