package at.fhv.cartservice.domain.exception;

import java.util.UUID;

public class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException(UUID cartItemId) {
        super("CartItem with ID " + cartItemId + " not found");
    }

    public CartItemNotFoundException(String message) {
        super(message);
    }
}

