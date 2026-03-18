package at.fhv.orderservice.rest.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GetOrderDTO(
        UUID id,
        UUID userId,
        String status,
        List<GetOrderItemDTO> items,
        BigDecimal totalPrice,
        LocalDateTime orderDate
) {
}
