package at.fhv.productservice.rest.dtos;

import java.math.BigDecimal;
import java.util.UUID;


public record GetProductDTO(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        int stock,
        String status
) {}
