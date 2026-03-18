package at.fhv.cartservice.rest;

import at.fhv.cartservice.application.services.*;
import at.fhv.cartservice.rest.dtos.AddCartItemDTO;
import at.fhv.cartservice.rest.dtos.CreateCartRequestDTO;
import at.fhv.cartservice.rest.dtos.GetCartDTO;
import at.fhv.cartservice.rest.dtos.UpdateCartItemDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/carts")
public class CartRestController {
    private final GetCartService getCartService;
    private final CreateCartService createCartService;
    private final AddProductToCartService addProductToCartService;
    private final RemoveProductFromCartService removeProductFromCartService;
    private final UpdateCartItemQuantityService updateCartItemQuantityService;
    private final ClearCartService clearCartService;
    private final DeleteCartService deleteCartService;

    public CartRestController(GetCartService getCartService, CreateCartService createCartService, AddProductToCartService addProductToCartService, RemoveProductFromCartService removeProductFromCartService,
                              UpdateCartItemQuantityService updateCartItemQuantityService, ClearCartService clearCartService, DeleteCartService deleteCartService) {
        this.getCartService = getCartService;
        this.createCartService = createCartService;
        this.addProductToCartService = addProductToCartService;
        this.removeProductFromCartService = removeProductFromCartService;
        this.updateCartItemQuantityService = updateCartItemQuantityService;
        this.clearCartService = clearCartService;
        this.deleteCartService = deleteCartService;
    }

    @GetMapping
    public ResponseEntity<List<GetCartDTO>> getAllCarts() {
        List<GetCartDTO> carts = getCartService.getAllCarts();
        return ResponseEntity.ok(carts);
    }

    @PostMapping
    public ResponseEntity<GetCartDTO> createCartForUser(@Valid @RequestBody CreateCartRequestDTO request) {
        GetCartDTO createdCart = createCartService.createCartForUser(request.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCart);
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<GetCartDTO> getCartByCartId(@PathVariable UUID cartId) {
        GetCartDTO cart = getCartService.getCartByCartId(cartId);
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<GetCartDTO> getCartByUserId(@PathVariable UUID userId) {
        GetCartDTO cart = getCartService.getCartByUserId(userId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<GetCartDTO> addItemToCart(@PathVariable UUID cartId,
                                                    @Valid @RequestBody AddCartItemDTO addCartItemDTO) {
        GetCartDTO updatedCart = addProductToCartService.addItemToCart(cartId, addCartItemDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedCart);
    }

    @DeleteMapping("/{cartId}/items/{cartItemId}")
    public ResponseEntity<GetCartDTO> removeItemFromCart(@PathVariable UUID cartId,
                                                         @PathVariable UUID cartItemId) {
        GetCartDTO updatedCart = removeProductFromCartService.removeItemFromCart(cartId, cartItemId);
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<Void> clearCart(@PathVariable UUID cartId) {
        clearCartService.clearCart(cartId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<Void> deleteCart(@PathVariable UUID cartId) {
        deleteCartService.deleteCart(cartId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/items")
    public ResponseEntity<GetCartDTO> updateItemQuantity(@Valid @RequestBody UpdateCartItemDTO updateCartItemDTO) {
        GetCartDTO updatedCart = updateCartItemQuantityService.updateItemQuantity(updateCartItemDTO);
        return ResponseEntity.ok(updatedCart);
    }

}
