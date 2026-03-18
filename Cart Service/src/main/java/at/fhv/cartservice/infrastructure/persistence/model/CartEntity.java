package at.fhv.cartservice.infrastructure.persistence.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "cart")
public class CartEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItemEntity> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CartStatusEntity status;

    public CartEntity() {}

    public CartEntity(UUID id, UUID userId, List<CartItemEntity> items, CartStatusEntity status) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }


    public UUID getUserId() {
        return userId;
    }

    public List<CartItemEntity> getItems() {
        return items;
    }

    public CartStatusEntity getStatus() {
        return status;
    }
}
