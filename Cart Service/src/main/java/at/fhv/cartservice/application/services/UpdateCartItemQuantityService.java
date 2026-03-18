package at.fhv.cartservice.application.services;

import at.fhv.cartservice.rest.dtos.GetCartDTO;
import at.fhv.cartservice.rest.dtos.UpdateCartItemDTO;

public interface UpdateCartItemQuantityService {
    GetCartDTO updateItemQuantity(UpdateCartItemDTO updateCartItemDTO);
}
