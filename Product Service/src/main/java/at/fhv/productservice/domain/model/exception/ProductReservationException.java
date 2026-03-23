package at.fhv.productservice.domain.model.exception;

public class ProductReservationException extends RuntimeException {
    private final String productId;
    private final Integer quantity;

    public ProductReservationException(String message, String productId, Integer quantity) {
        super(message);
        this.productId = productId;
        this.quantity = quantity;
    }

    public ProductReservationException(String message, String productId, Integer quantity, Throwable cause) {
        super(message, cause);
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
