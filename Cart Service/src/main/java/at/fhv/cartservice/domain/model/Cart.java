package at.fhv.cartservice.domain.model;

import at.fhv.cartservice.domain.exception.CartItemNotFoundException;
import at.fhv.cartservice.domain.exception.InvalidCartDataException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Cart {
    private UUID id;
    private UUID userId;
    private List<CartItem> items;
    private CartStatus status;

    private Cart(UUID id, UUID userId, List<CartItem> items, CartStatus cartStatus) {
        this.id = id;
        this.userId = userId;
        this.items = new ArrayList<>(items);
        this.status = cartStatus;
    }

    public static Cart create(UUID userId) {
        return new Cart(UUID.randomUUID(), userId, new ArrayList<>(), CartStatus.ACTIVE);
    }

    public void delete() {
        if (this.status == CartStatus.ACTIVE) {
            this.status = CartStatus.INACTIVE;
        } else {
            throw new InvalidCartDataException("CartId", this.id, "Only carts in ACTIVE status can be cancelled.");
        }
    }

    public static Cart reconstruct(UUID id, UUID userId, List<CartItem> items, CartStatus status) {
        return new Cart(id, userId, items, status);
    }

    public void addItem(UUID productId, int quantity) {
        if (this.status == CartStatus.INACTIVE) {
            throw new InvalidCartDataException("CartId", this.id, "Items can be added to only carts with ACTIVE status.");
        }
        for (CartItem existingItem : items) {
            if (existingItem.getProductId().equals(productId)) {
                existingItem.increaseQuantity(quantity);
                return;
            }
        }

        CartItem newItem = CartItem.create(productId, quantity);
        items.add(newItem);
    }

    public boolean isInactive() {
        return this.status == CartStatus.INACTIVE;
    }

    public void removeItem(UUID cartItemId) {
        CartItem itemToRemove = findItemById(cartItemId);
        items.remove(itemToRemove);
    }

    public void updateItemQuantity(UUID cartItemId, int newQuantity) {
        CartItem item = findItemById(cartItemId);
        item.setQuantity(newQuantity);
    }

    public void clear() {
        items.clear();
    }

    private CartItem findItemById(UUID cartItemId) {
        for (CartItem item : items) {
            if (item.getId().equals(cartItemId)) {
                return item;
            }
        }
        throw new CartItemNotFoundException(cartItemId);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public CartStatus getStatus() {
        return status;
    }
}
