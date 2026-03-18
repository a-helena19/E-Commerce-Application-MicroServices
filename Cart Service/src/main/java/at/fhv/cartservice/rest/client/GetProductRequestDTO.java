package at.fhv.cartservice.rest.client;

import java.util.UUID;

public class GetProductRequestDTO {
    private UUID id;

    public GetProductRequestDTO() {}

    public GetProductRequestDTO(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}

