package at.fhv.productservice.application.services;

import at.fhv.productservice.rest.dtos.CreateProductDTO;
import at.fhv.productservice.rest.dtos.GetProductDTO;

public interface CreateProductService {
    GetProductDTO createProduct(CreateProductDTO dto);
}
