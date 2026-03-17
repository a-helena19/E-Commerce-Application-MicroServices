package at.fhv.cartservice.application.services;

import java.util.UUID;

public interface DeleteCartService {
    void deleteCart(UUID cartId);
    void deleteCartByUserId(UUID userId);
}
