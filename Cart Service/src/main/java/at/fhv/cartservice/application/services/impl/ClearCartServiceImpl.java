package at.fhv.cartservice.application.services.impl;

import at.fhv.cartservice.application.services.ClearCartService;
import at.fhv.cartservice.domain.model.Cart;
import at.fhv.cartservice.domain.model.CartRepository;
import at.fhv.cartservice.domain.exception.CartNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ClearCartServiceImpl implements ClearCartService {
    private final CartRepository cartRepository;

    public ClearCartServiceImpl(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Override
    public void clearCart(UUID cartId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new CartNotFoundException(cartId));
        cart.clear();
        cartRepository.save(cart);
    }

    @Override
    public void clearCartByUserId(UUID userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart != null) {
            cart.clear();
            cartRepository.save(cart);
        }
    }
}
