package at.fhv.cartservice.application.mapper.dtoMapper;

import at.fhv.cartservice.domain.model.Cart;
import at.fhv.cartservice.rest.dtos.GetCartDTO;

public interface CartDTOMapper {
    GetCartDTO toGetCartDTO(Cart cart);
    Cart toDomainFromGetCartDTO(GetCartDTO getCartDTO);
}
