package at.fhv.userservice.rest.client;

import java.util.List;
import java.util.UUID;

public class CreateCartResponseDTO {
    private UUID cartId;
    private UUID userId;
    private List<CartItemDTO> items;
    private String status;

    public CreateCartResponseDTO() {}

    public CreateCartResponseDTO(UUID cartId, UUID userId, List<CartItemDTO> items, String status) {
        this.cartId = cartId;
        this.userId = userId;
        this.items = items;
        this.status = status;
    }

    public UUID getCartId() {
        return cartId;
    }

    public void setCartId(UUID cartId) {
        this.cartId = cartId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public List<CartItemDTO> getItems() {
        return items;
    }

    public void setItems(List<CartItemDTO> items) {
        this.items = items;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Helper method for backwards compatibility
    public UUID getId() {
        return cartId;
    }

    public static class CartItemDTO {
        private UUID cartItemId;
        private UUID productId;
        private int quantity;

        public CartItemDTO() {}

        public CartItemDTO(UUID cartItemId, UUID productId, int quantity) {
            this.cartItemId = cartItemId;
            this.productId = productId;
            this.quantity = quantity;
        }

        public UUID getCartItemId() {
            return cartItemId;
        }

        public void setCartItemId(UUID cartItemId) {
            this.cartItemId = cartItemId;
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
}
