package at.fhv.productservice.domain.model.events;
import java.util.UUID;

public class OrderItemEvent {
    private UUID productId;
    private int quantity;

    public OrderItemEvent() {
    }

    public OrderItemEvent(UUID productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
