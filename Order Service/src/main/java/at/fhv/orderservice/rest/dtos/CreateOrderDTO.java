package at.fhv.orderservice.rest.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CreateOrderDTO {

    @NotNull(message = "User ID is required")
    private UUID userId;

    public UUID getUserId() {
        return userId;
    }
}
