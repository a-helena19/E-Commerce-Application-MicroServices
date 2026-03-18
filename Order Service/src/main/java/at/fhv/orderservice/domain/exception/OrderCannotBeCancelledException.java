package at.fhv.orderservice.domain.exception;

import java.util.UUID;

public class OrderCannotBeCancelledException extends RuntimeException {
    private final UUID orderId;

    public OrderCannotBeCancelledException(UUID orderId, String reason) {
        super("Order with id " + orderId + " cannot be cancelled: " + reason);
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }
}

