package at.fhv.orderservice.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderItem {
    private UUID id;
    private UUID productId;
    private int quantity;
    private BigDecimal price;

    public OrderItem(UUID id, UUID productId, int quantity, BigDecimal price) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
