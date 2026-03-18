package at.fhv.cartservice.application.mapper.dtoMapper;

import at.fhv.cartservice.domain.model.Cart;
import at.fhv.cartservice.domain.model.CartItem;
import at.fhv.cartservice.domain.model.CartStatus;
import at.fhv.cartservice.rest.dtos.GetCartDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CartDTOMapperImpl implements CartDTOMapper {
    @Override
    public GetCartDTO toGetCartDTO(Cart cart) {
        List<GetCartDTO.GetCartItemDTO> items = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            items.add(new GetCartDTO.GetCartItemDTO(item.getId(), item.getProductId(), item.getQuantity()));
        }

        return new GetCartDTO(
                cart.getId(),
                cart.getUserId(),
                items,
                cart.getStatus().name()
        );
    }

    @Override
    public Cart toDomainFromGetCartDTO(GetCartDTO getCartDTO) {
        List<CartItem> items = new ArrayList<>();

        for (GetCartDTO.GetCartItemDTO itemDTO : getCartDTO.items()) {
            items.add(CartItem.reconstruct(
                    null,
                    itemDTO.productId(),
                    itemDTO.quantity()
            ));
        }

        return Cart.reconstruct(
                getCartDTO.cartId(),
                getCartDTO.userId(),
                items,
                CartStatus.valueOf(getCartDTO.status())
        );
    }
}
