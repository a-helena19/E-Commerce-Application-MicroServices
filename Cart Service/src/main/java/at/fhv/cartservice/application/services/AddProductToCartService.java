package at.fhv.cartservice.application.services;

import at.fhv.cartservice.rest.dtos.AddCartItemDTO;
import at.fhv.cartservice.rest.dtos.GetCartDTO;

import java.util.UUID;

public interface AddProductToCartService {
    GetCartDTO addItemToCart(UUID cartId, AddCartItemDTO addCartItemDTO);
}
