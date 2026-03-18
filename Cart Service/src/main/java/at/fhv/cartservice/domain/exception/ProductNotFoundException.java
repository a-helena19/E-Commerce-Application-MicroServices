package at.fhv.cartservice.domain.exception;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(UUID productId) {
        super("Product with ID " + productId + " not found");
    }

    public ProductNotFoundException(String message) {
        super(message);
    }
}

