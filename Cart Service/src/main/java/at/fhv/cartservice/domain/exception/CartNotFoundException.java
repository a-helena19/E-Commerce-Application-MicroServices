package at.fhv.cartservice.domain.exception;

import java.util.UUID;

public class CartNotFoundException extends RuntimeException {
    private final UUID id;

    public CartNotFoundException(UUID id) {
        super("Cart not found for id: " + id);
        this.id = id;
    }

    public static CartNotFoundException byCartId(UUID cartId) {
        return new CartNotFoundException("Cart with cart id " + cartId + " was not found.", cartId);
    }

    public static CartNotFoundException byUserId(UUID userId) {
        return new CartNotFoundException("Cart for user id " + userId + " was not found.", userId);
    }

    private CartNotFoundException(String message, UUID id) {
        super(message);
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
