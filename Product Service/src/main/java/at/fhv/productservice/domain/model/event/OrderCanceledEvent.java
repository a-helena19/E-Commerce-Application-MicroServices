package at.fhv.productservice.domain.model.event;

import java.time.LocalDateTime;

public class OrderCanceledEvent {

    private String orderId;
    private String productId;
    private Integer quantity;
    private LocalDateTime timestamp;

    public OrderCanceledEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public OrderCanceledEvent(String orderId, String productId, Integer quantity) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.timestamp = LocalDateTime.now();
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "OrderCanceledEvent{" +
                "orderId='" + orderId + '\'' +
                ", productId='" + productId + '\'' +
                ", quantity=" + quantity +
                ", timestamp=" + timestamp +
                '}';
    }
}