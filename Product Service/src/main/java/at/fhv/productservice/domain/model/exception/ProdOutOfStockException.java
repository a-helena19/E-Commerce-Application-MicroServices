package at.fhv.productservice.domain.model.exception;

import java.util.UUID;

public class ProdOutOfStockException extends RuntimeException {
    private final UUID productId;
    private final int requestedQuantity;
    private final int availableStock;

    public ProdOutOfStockException(UUID productId, int requestedQuantity, int availableStock) {
        super("Product " + productId + " is out of stock. " +
                "Requested: " + requestedQuantity +
                ", Available: " + availableStock);
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
    }

    public UUID getProductId() {
        return productId;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableStock() {
        return availableStock;
    }
}
