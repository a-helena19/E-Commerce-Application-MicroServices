package at.fhv.orderservice.infrastructure.persistence.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatusEntity status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> orderItems;

    @Column(name = "total_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    public OrderEntity() {
    }

    public OrderEntity(UUID id, UUID userId, OrderStatusEntity status, List<OrderItemEntity> orderItems, BigDecimal totalPrice, LocalDateTime orderDate) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.orderItems = orderItems;
        this.totalPrice = totalPrice;
        this.orderDate = orderDate;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public OrderStatusEntity getStatus() {
        return status;
    }

    public List<OrderItemEntity> getOrderItems() {
        return orderItems;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }


}
