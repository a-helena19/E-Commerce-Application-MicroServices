package at.fhv.userservice.rest.client;

import java.util.UUID;

public class CreateCartResponseDTO {
    private UUID id;

    public CreateCartResponseDTO() {}

    public CreateCartResponseDTO(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
