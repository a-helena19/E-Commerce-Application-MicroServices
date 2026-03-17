package at.fhv.api_gateway.rest.dtos.products;

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
