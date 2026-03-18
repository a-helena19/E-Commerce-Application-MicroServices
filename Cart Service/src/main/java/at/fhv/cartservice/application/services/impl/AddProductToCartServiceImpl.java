package at.fhv.cartservice.application.services.impl;

import at.fhv.cartservice.application.mapper.dtoMapper.CartDTOMapper;
import at.fhv.cartservice.application.services.AddProductToCartService;
import at.fhv.cartservice.domain.model.Cart;
import at.fhv.cartservice.domain.model.CartItem;
import at.fhv.cartservice.domain.model.CartRepository;
import at.fhv.cartservice.domain.exception.CartNotFoundException;
import at.fhv.cartservice.domain.exception.InvalidCartItemDataException;
import at.fhv.cartservice.rest.client.ProductServiceClient;
import at.fhv.cartservice.rest.client.GetProductResponseDTO;
import at.fhv.cartservice.rest.dtos.AddCartItemDTO;
import at.fhv.cartservice.rest.dtos.GetCartDTO;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AddProductToCartServiceImpl implements AddProductToCartService {
    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;
    private final CartDTOMapper cartDTOMapper;

    public AddProductToCartServiceImpl(CartRepository cartRepository, ProductServiceClient productServiceClient, CartDTOMapper cartDTOMapper) {
        this.cartRepository = cartRepository;
        this.productServiceClient = productServiceClient;
        this.cartDTOMapper = cartDTOMapper;
    }

    @Override
    public GetCartDTO addItemToCart(UUID cartId, AddCartItemDTO addCartItemDTO) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> CartNotFoundException.byCartId(cartId));
        UUID productId = addCartItemDTO.getProductId();
        int quantity = addCartItemDTO.getQuantity();

        GetProductResponseDTO product = productServiceClient.getProductById(productId);

        if (!product.getStatus().equalsIgnoreCase("ACTIVE")) {
            throw new InvalidCartItemDataException("productId", productId, "Product is not available");
        }

        // Calculate total quantity (existing + new)
        int existingQuantity = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .mapToInt(CartItem::getQuantity)
                .sum();
        int totalQuantity = existingQuantity + quantity;

        if (product.getStock() < totalQuantity) {
            throw new RuntimeException("Product out of stock. Requested: " + totalQuantity + ", Available: " + product.getStock());
        }

        cart.addItem(productId, quantity);

        Cart savedCart = cartRepository.save(cart);
        return cartDTOMapper.toGetCartDTO(savedCart);
    }
}
