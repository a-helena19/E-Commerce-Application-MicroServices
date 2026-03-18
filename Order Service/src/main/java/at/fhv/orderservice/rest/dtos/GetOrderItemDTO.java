package at.fhv.orderservice.rest.dtos;


import java.math.BigDecimal;
import java.util.UUID;

public record GetOrderItemDTO(
        UUID id,
        UUID productId,
        int quantity,
        BigDecimal price
){}
