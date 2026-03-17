package at.fhv.orderservice.domain.exception;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(UUID orderId) {
        super("Order with ID " + orderId + " not found");
    }

    public OrderNotFoundException(String message) {
        super(message);
    }
}

