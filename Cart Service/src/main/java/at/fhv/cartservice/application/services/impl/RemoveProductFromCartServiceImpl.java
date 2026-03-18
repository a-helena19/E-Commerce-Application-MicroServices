package at.fhv.cartservice.application.services.impl;

import at.fhv.cartservice.application.mapper.dtoMapper.CartDTOMapper;
import at.fhv.cartservice.application.services.RemoveProductFromCartService;
import at.fhv.cartservice.domain.model.Cart;
import at.fhv.cartservice.domain.model.CartRepository;
import at.fhv.cartservice.domain.exception.CartNotFoundException;
import at.fhv.cartservice.rest.dtos.GetCartDTO;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RemoveProductFromCartServiceImpl implements RemoveProductFromCartService {
    private final CartRepository cartRepository;
    private final CartDTOMapper cartDTOMapper;

    public RemoveProductFromCartServiceImpl(CartRepository cartRepository, CartDTOMapper cartDTOMapper) {
        this.cartRepository = cartRepository;
        this.cartDTOMapper = cartDTOMapper;
    }

    @Override
    public GetCartDTO removeItemFromCart(UUID cartId, UUID cartItemId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> CartNotFoundException.byCartId(cartId));
        cart.removeItem(cartItemId);
        Cart savedCart = cartRepository.save(cart);
        return cartDTOMapper.toGetCartDTO(savedCart);
    }
}
