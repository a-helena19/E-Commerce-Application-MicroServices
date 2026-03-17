package at.fhv.productservice.application.services;

import at.fhv.productservice.rest.dtos.GetProductDTO;

import java.util.UUID;

public interface DeleteProductService {
    GetProductDTO deleteProduct(UUID id);
}
