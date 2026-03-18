package at.fhv.cartservice.infrastructure.mapper;

import at.fhv.cartservice.application.mapper.CartMapper;
import at.fhv.cartservice.domain.model.Cart;
import at.fhv.cartservice.domain.model.CartItem;
import at.fhv.cartservice.domain.model.CartStatus;
import at.fhv.cartservice.infrastructure.persistence.model.CartEntity;
import at.fhv.cartservice.infrastructure.persistence.model.CartItemEntity;
import at.fhv.cartservice.infrastructure.persistence.model.CartStatusEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CartMapperImpl implements CartMapper {
    @Override
    public Cart toDomain(CartEntity entity) {
        List<CartItem> items = new ArrayList<>();
        for (CartItemEntity itemEntity : entity.getItems()) {
            items.add(toDomainItem(itemEntity));
        }

        return Cart.reconstruct(
                entity.getId(),
                entity.getUserId(),
                items,
                CartStatus.valueOf(entity.getStatus().name())
        );
    }

    @Override
    public CartEntity toEntity(Cart cart) {
        List<CartItemEntity> items = new ArrayList<>();

        CartEntity cartEntity = new CartEntity(
                cart.getId(),
                cart.getUserId(),
                items,
                CartStatusEntity.valueOf(cart.getStatus().name())
        );

        for (CartItem item : cart.getItems()) {
            CartItemEntity itemEntity = toEntityItem(item);
            itemEntity.setCart(cartEntity);
            items.add(itemEntity);
        }

        return cartEntity;
    }

    @Override
    public CartItem toDomainItem(CartItemEntity entity) {
        return CartItem.reconstruct(
                entity.getId(),
                entity.getProductId(),
                entity.getQuantity()
        );
    }

    @Override
    public CartItemEntity toEntityItem(CartItem cartItem) {
        return new CartItemEntity(
                cartItem.getId(),
                cartItem.getProductId(),
                cartItem.getQuantity()
        );
    }

}
