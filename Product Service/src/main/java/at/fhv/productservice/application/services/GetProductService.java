package at.fhv.productservice.application.services;

import at.fhv.productservice.rest.dtos.GetProductDTO;

import java.util.List;
import java.util.UUID;

public interface GetProductService {
    GetProductDTO getProductById(UUID id);
    List<GetProductDTO> getAllProducts();
    List<GetProductDTO> getAllProducts(boolean includeInactive);
}
