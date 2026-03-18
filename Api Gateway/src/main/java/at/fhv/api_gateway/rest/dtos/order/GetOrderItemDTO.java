package at.fhv.api_gateway.rest.dtos.order;


import java.math.BigDecimal;
import java.util.UUID;

public record GetOrderItemDTO(
        UUID id,
        UUID productId,
        int quantity,
        BigDecimal price
){}
