package at.fhv.cartservice.application.services;

import at.fhv.cartservice.rest.dtos.GetCartDTO;

import java.util.UUID;

public interface RemoveProductFromCartService {
    GetCartDTO removeItemFromCart(UUID cartId, UUID cartItemId);
}
