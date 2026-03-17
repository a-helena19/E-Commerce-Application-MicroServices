package at.fhv.cartservice.application.services.impl;

import at.fhv.cartservice.application.mapper.dtoMapper.CartDTOMapper;
import at.fhv.cartservice.application.services.UpdateCartItemQuantityService;
import at.fhv.cartservice.domain.model.Cart;
import at.fhv.cartservice.domain.model.CartItem;
import at.fhv.cartservice.domain.model.CartRepository;
import at.fhv.cartservice.domain.exception.CartItemNotFoundException;
import at.fhv.cartservice.domain.exception.CartNotFoundException;
import at.fhv.cartservice.rest.client.ProductServiceClient;
import at.fhv.cartservice.rest.client.GetProductResponseDTO;
import at.fhv.cartservice.rest.dtos.GetCartDTO;
import at.fhv.cartservice.rest.dtos.UpdateCartItemDTO;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateCartItemQuantityServiceImpl implements UpdateCartItemQuantityService {
    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;
    private final CartDTOMapper cartDTOMapper;

    public UpdateCartItemQuantityServiceImpl(CartRepository cartRepository, ProductServiceClient productServiceClient, CartDTOMapper cartDTOMapper) {
        this.cartRepository = cartRepository;
        this.productServiceClient = productServiceClient;
        this.cartDTOMapper = cartDTOMapper;
    }

    @Override
    public GetCartDTO updateItemQuantity(UpdateCartItemDTO updateCartItemDTO) {
        Cart cart = cartRepository.findById(updateCartItemDTO.getCartId()).orElseThrow(() -> CartNotFoundException.byCartId(updateCartItemDTO.getCartId()));
        UUID foundProductId = null;

        for (CartItem item : cart.getItems()) {
            if (item.getId().equals(updateCartItemDTO.getCartItemId())) {
                foundProductId = item.getProductId();
                break;
            }
        }

        if (foundProductId == null) {
            throw new CartItemNotFoundException(updateCartItemDTO.getCartItemId());
        }

        UUID productId = foundProductId;
        GetProductResponseDTO product = productServiceClient.getProductById(productId);

        int newQuantity = updateCartItemDTO.getQuantity();
        if (product.getStock() < newQuantity) {
            throw new RuntimeException("Product out of stock. Requested: " + newQuantity + ", Available: " + product.getStock());
        }

        cart.updateItemQuantity(updateCartItemDTO.getCartItemId(), newQuantity);
        Cart savedCart = cartRepository.save(cart);
        return cartDTOMapper.toGetCartDTO(savedCart);
    }
}
