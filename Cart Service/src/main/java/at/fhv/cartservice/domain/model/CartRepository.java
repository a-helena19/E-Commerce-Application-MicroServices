package at.fhv.cartservice.domain.model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartRepository {
    Cart save(Cart cart);
    Cart findByUserId(UUID userId);
    Optional<Cart> findById(UUID cartId);
    List<Cart> findAll();
    void deleteByUserId(UUID userId);
}
