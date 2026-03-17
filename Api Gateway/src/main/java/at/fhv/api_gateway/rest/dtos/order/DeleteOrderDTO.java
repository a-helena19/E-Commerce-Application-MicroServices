package at.fhv.api_gateway.rest.dtos.order;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class DeleteOrderDTO {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    public UUID getOrderId() {
        return orderId;
    }
}
