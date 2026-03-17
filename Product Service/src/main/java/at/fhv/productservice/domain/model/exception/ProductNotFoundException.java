package at.fhv.productservice.domain.model.exception;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {
    private final UUID productId;

    public ProductNotFoundException(UUID productId) {
        super("Product with id " + productId + " was not found.");
        this.productId = productId;
    }

    public UUID getProductId() {
        return productId;
    }
}
