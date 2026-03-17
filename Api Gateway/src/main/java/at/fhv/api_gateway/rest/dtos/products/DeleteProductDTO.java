package at.fhv.api_gateway.rest.dtos.products;

import java.util.UUID;

public class DeleteProductDTO {
    private UUID id;

    public DeleteProductDTO() {
    }

    public DeleteProductDTO(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
