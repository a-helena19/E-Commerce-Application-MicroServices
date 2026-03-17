package at.fhv.productservice.application.services;

import at.fhv.productservice.rest.dtos.GetProductDTO;
import at.fhv.productservice.rest.dtos.UpdateProductDTO;

import java.util.UUID;

public interface UpdateProductService {
    GetProductDTO updateProduct(UUID id, UpdateProductDTO dto);
}
