package at.fhv.productservice.domain.model.event;

import at.fhv.productservice.domain.model.event.OrderItemEvent;

import java.util.List;
import java.util.UUID;

public class OrderCanceledEvent {

    private UUID orderId;
    private UUID userId;
    private List<OrderItemEvent> orderItems;
    private long timestamp;

    public OrderCanceledEvent() {
    }

    public OrderCanceledEvent(UUID orderId, UUID userId, List<OrderItemEvent> orderItems) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderItems = orderItems;
        this.timestamp = System.currentTimeMillis();
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public List<OrderItemEvent> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItemEvent> orderItems) {
        this.orderItems = orderItems;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}