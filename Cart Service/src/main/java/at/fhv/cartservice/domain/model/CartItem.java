package at.fhv.cartservice.domain.model;

import at.fhv.cartservice.domain.exception.InvalidCartItemDataException;

import java.util.UUID;

public class CartItem {
    private UUID id;
    private UUID productId;
    private int quantity;

    private CartItem(UUID id, UUID productId, int quantity) {
        validateQuantity(quantity);
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
    }

    public static CartItem create(UUID productId, int quantity) {
        return new CartItem(UUID.randomUUID(), productId, quantity);
    }

    public static CartItem reconstruct(UUID id, UUID productId, int quantity) {
        return new CartItem(id, productId, quantity);
    }

    public void increaseQuantity(int amount) {
        validateQuantity(amount);
        quantity += amount;
    }

    public void setQuantity(int newQuantity) {
        validateQuantity(newQuantity);
        quantity = newQuantity;
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new InvalidCartItemDataException("quantity", quantity, "Quantity must be greater than zero.");
        }
    }

    public UUID getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public UUID getId() {
        return id;
    }
}
