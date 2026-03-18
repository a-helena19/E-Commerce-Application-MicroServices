package at.fhv.cartservice.application.mapper;

import at.fhv.cartservice.domain.model.Cart;
import at.fhv.cartservice.domain.model.CartItem;
import at.fhv.cartservice.infrastructure.persistence.model.CartEntity;
import at.fhv.cartservice.infrastructure.persistence.model.CartItemEntity;

public interface CartMapper {
    Cart toDomain(CartEntity entity);
    CartEntity toEntity(Cart cart);
    CartItem toDomainItem(CartItemEntity entity);
    CartItemEntity toEntityItem(CartItem item);
}
