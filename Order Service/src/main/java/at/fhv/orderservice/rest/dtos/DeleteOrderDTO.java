package at.fhv.orderservice.rest.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class DeleteOrderDTO {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    public UUID getOrderId() {
        return orderId;
    }
}
