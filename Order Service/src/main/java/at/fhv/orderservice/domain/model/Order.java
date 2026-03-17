package at.fhv.orderservice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Order {
    private UUID id;
    private UUID userId;
    private OrderStatus status;
    private List<OrderItem> orderItems;
    private BigDecimal totalPrice;
    private LocalDateTime orderDate;

    public Order(UUID id, UUID userId, OrderStatus status, List<OrderItem> orderItems, BigDecimal totalPrice, LocalDateTime orderDate) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.orderItems = orderItems != null ? orderItems : new java.util.ArrayList<>();
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void markAsCreated() {
        this.status = OrderStatus.CREATED;
    }

    public void markAsCancelled() {
        this.status = OrderStatus.CANCELLED;
    }

    public boolean hasItems() {
        return !orderItems.isEmpty();
    }

    public boolean isCreated() {
        return status == OrderStatus.CREATED;
    }

    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED;
    }

    public void delete() {
        if (isCreated()) {
            markAsCancelled();
        } else {
            throw new IllegalStateException("Only orders in CREATED status can be cancelled.");
        }
    }

    public static Order create(UUID userId, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        Order order = new Order(
                null,
                userId,
                OrderStatus.CREATED,
                items,
                BigDecimal.ZERO,
                LocalDateTime.now()
        );

        order.recalculateTotalPrice();

        return order;
    }

    private void recalculateTotalPrice() {
        totalPrice = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalPrice = totalPrice.add(itemTotal);
        }
    }

}
