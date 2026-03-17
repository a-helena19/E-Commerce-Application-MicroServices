package at.fhv.cartservice.rest.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CreateCartRequestDTO {
    @NotNull(message = "User ID is required")
    private UUID userId;

    public CreateCartRequestDTO() {}

    public CreateCartRequestDTO(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}

