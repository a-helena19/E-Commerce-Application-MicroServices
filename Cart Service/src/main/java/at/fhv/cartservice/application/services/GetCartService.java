package at.fhv.cartservice.application.services;

import at.fhv.cartservice.rest.dtos.GetCartDTO;

import java.util.List;
import java.util.UUID;

public interface GetCartService {
    GetCartDTO getCartByUserId(UUID userId);
    GetCartDTO getCartByCartId(UUID cartId);
    List<GetCartDTO> getAllCarts();
}
