package at.fhv.api_gateway.rest.dtos.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class UpdateCartItemDTO {
    @NotNull(message = "Cart ID is required")
    private UUID cartId;

    @NotNull(message = "Cart Item ID is required")
    private UUID cartItemId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    public UpdateCartItemDTO() {
    }

    public UpdateCartItemDTO(UUID cardId, UUID cardItemId, int quantity) {
        this.cartId = cardId;
        this.cartItemId = cardItemId;
        this.quantity = quantity;
    }

    public UUID getCartId() {
        return cartId;
    }

    public void setCartId(UUID cartId) {
        this.cartId = cartId;
    }

    public UUID getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(UUID cartItemId) {
        this.cartItemId = cartItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
