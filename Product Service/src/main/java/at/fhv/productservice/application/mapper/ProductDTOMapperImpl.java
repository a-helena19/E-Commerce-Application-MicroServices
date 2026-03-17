package at.fhv.productservice.application.mapper;

import at.fhv.productservice.domain.model.Product;
import at.fhv.productservice.rest.dtos.CreateProductDTO;
import at.fhv.productservice.rest.dtos.GetProductDTO;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProductDTOMapperImpl implements ProductDTOMapper {

    @Override
    public GetProductDTO toGetProductDTO(Product product) {
        return new GetProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getStatus().name()
        );
    }

    @Override
    public Product toDomainCreateProductDTO(CreateProductDTO createProductDTO) {
        return Product.create(
                UUID.randomUUID(),
                createProductDTO.getName(),
                createProductDTO.getDescription(),
                createProductDTO.getPrice(),
                createProductDTO.getStock()
        );
    }
}
