package at.fhv.cartservice.application.services.impl;

import at.fhv.cartservice.application.services.DeleteCartService;
import at.fhv.cartservice.domain.model.Cart;
import at.fhv.cartservice.domain.model.CartRepository;
import at.fhv.cartservice.domain.exception.CartNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteCartServiceImpl implements DeleteCartService {
    private final CartRepository cartRepository;

    public DeleteCartServiceImpl(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Override
    public void deleteCart(UUID cartId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> CartNotFoundException.byCartId(cartId));
        cart.delete();
        cartRepository.save(cart);
    }

    @Override
    public void deleteCartByUserId(UUID userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            throw CartNotFoundException.byUserId(userId);
        }
        cart.delete();
        cartRepository.save(cart);
    }
}
