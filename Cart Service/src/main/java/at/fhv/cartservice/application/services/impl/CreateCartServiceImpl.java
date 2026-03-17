package at.fhv.cartservice.application.services.impl;

import at.fhv.cartservice.application.mapper.dtoMapper.CartDTOMapper;
import at.fhv.cartservice.application.services.CreateCartService;
import at.fhv.cartservice.domain.model.Cart;
import at.fhv.cartservice.domain.model.CartRepository;
import at.fhv.cartservice.rest.dtos.GetCartDTO;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateCartServiceImpl implements CreateCartService {
    private final CartRepository cartRepository;
    private final CartDTOMapper cartDTOMapper;

    public CreateCartServiceImpl(CartRepository cartRepository, CartDTOMapper cartDTOMapper) {
        this.cartRepository = cartRepository;
        this.cartDTOMapper = cartDTOMapper;
    }

    @Override
    public GetCartDTO createCartForUser(UUID userId) {
        Cart cart = Cart.create(userId);
        Cart savedCart = cartRepository.save(cart);
        return cartDTOMapper.toGetCartDTO(savedCart);
    }
}

