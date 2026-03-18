package at.fhv.cartservice.infrastructure.persistence.model;

import at.fhv.cartservice.application.mapper.CartMapper;
import at.fhv.cartservice.domain.model.Cart;
import at.fhv.cartservice.domain.model.CartRepository;
import at.fhv.cartservice.domain.exception.CartNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CartRepositoryImpl implements CartRepository {
    private final CartMapper cartMapper;
    private final CartJpaRepository jpaRepository;

    public CartRepositoryImpl(CartMapper cartMapper, CartJpaRepository jpaRepository) {
        this.cartMapper = cartMapper;
        this.jpaRepository = jpaRepository;
    }


    @Override
    public Cart save (Cart cart) {
        var entity = cartMapper.toEntity(cart);
        var saved = jpaRepository.save(entity);
        return cartMapper.toDomain(saved);
    }

    @Override
    public Cart findByUserId (UUID userId) {
        return jpaRepository.findByUserId(userId)
                .map(cartMapper::toDomain)
                .orElseThrow(() -> CartNotFoundException.byUserId(userId));
    }

    @Override
    public Optional<Cart> findById(UUID cartId) {
        return jpaRepository.findById(cartId)
                .map(cartMapper::toDomain);
    }

    @Override
    public List<Cart> findAll() {
        List<CartEntity> entities = jpaRepository.findAll();
        List<Cart> carts = new ArrayList<>();

        for (CartEntity entity : entities) {
            carts.add(cartMapper.toDomain(entity));
        }

        return carts;
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jpaRepository.deleteByUserId(userId);
    }

}
