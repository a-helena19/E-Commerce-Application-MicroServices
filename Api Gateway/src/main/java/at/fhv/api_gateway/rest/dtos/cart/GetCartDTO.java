package at.fhv.api_gateway.rest.dtos.cart;

import java.util.List;
import java.util.UUID;

public record GetCartDTO(
        UUID cartId,
        UUID userId,
        List<GetCartItemDTO> items,
        String status
) {

    public record GetCartItemDTO(
            UUID cartItemId,
            UUID productId,
            int quantity
    ) {}

}
