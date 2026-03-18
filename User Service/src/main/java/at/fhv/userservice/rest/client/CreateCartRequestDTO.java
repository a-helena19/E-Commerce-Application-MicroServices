package at.fhv.userservice.rest.client;

import java.util.UUID;

public class CreateCartRequestDTO {
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

