package at.fhv.cartservice.domain.exception;

import java.util.UUID;

public class ProductOutOfStockException extends RuntimeException {
    public ProductOutOfStockException(UUID productId, int requestedQuantity, int availableStock) {
        super("Product with ID " + productId + " is out of stock. Requested: " + requestedQuantity + ", Available: " + availableStock);
    }

    public ProductOutOfStockException(String message) {
        super(message);
    }
}

