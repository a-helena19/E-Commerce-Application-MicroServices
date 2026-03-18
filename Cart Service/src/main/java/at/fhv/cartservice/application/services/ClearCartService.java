package at.fhv.cartservice.application.services;

import java.util.UUID;

public interface ClearCartService {
    void clearCart(UUID cartId);
    void clearCartByUserId(UUID userId);
}
