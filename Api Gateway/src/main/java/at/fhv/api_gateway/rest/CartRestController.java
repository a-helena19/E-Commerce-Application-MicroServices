package at.fhv.api_gateway.rest;

import at.fhv.api_gateway.application.config.RestClientConfig;
import at.fhv.api_gateway.rest.dtos.cart.AddCartItemDTO;
import at.fhv.api_gateway.rest.dtos.cart.GetCartDTO;
import at.fhv.api_gateway.rest.dtos.cart.UpdateCartItemDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/carts")
public class CartRestController {


    private final RestTemplate restTemplate;
    private final RestClientConfig restClientConfig;

    public CartRestController(RestTemplate restTemplate, RestClientConfig restClientConfig) {
        this.restTemplate = restTemplate;
        this.restClientConfig = restClientConfig;
    }

    @Operation(description = "Retrieves all carts. Carts are automatically created when a user is created, so there is one cart per user.")
    @GetMapping
    public ResponseEntity<List<GetCartDTO>> getAllCarts() {
        String url = restClientConfig.cartServiceUrl + "/carts";
        return restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<List<GetCartDTO>>(){});
    }

    @Operation(description = "Retrieves the cart by cart ID.")
    @GetMapping("/{cartId}")
    public ResponseEntity<GetCartDTO> getCartByCartId(@PathVariable UUID cartId) {
        String url = restClientConfig.cartServiceUrl + "/carts/" + cartId;
        return restTemplate.getForEntity(url, GetCartDTO.class);
    }

    @Operation(description = "Retrieves the cart by user ID.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<GetCartDTO> getCartByUserId(@PathVariable UUID userId) {
        String url = restClientConfig.cartServiceUrl + "/carts/user/" + userId;
        return restTemplate.getForEntity(url, GetCartDTO.class);
    }

    @Operation(description = "Adds a specified product to a specified cart with a given quantity.")
    @PostMapping("/{cartId}/items")
    public ResponseEntity<GetCartDTO> addItemToCart(@PathVariable UUID cartId,
                                                    @Valid @RequestBody AddCartItemDTO addCartItemDTO) {
        String url = restClientConfig.cartServiceUrl + "/carts/" + cartId + "/items";
        HttpEntity<AddCartItemDTO> requestEntity = new HttpEntity<>(addCartItemDTO);
        return restTemplate.postForEntity(url, requestEntity, GetCartDTO.class);
    }

    @Operation(description = "Removes a specific item from a specified cart.")
    @DeleteMapping("/{cartId}/items/{cartItemId}")
    public ResponseEntity<GetCartDTO> removeItemFromCart(@PathVariable UUID cartId,
                                                         @PathVariable UUID cartItemId) {
        String url = restClientConfig.cartServiceUrl + "/carts/" + cartId + "/items/" + cartItemId;
        return restTemplate.exchange(url, HttpMethod.DELETE, HttpEntity.EMPTY, GetCartDTO.class);
    }

    @Operation(description = "Removes all items from a specified cart.")
    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<Void> clearCart(@PathVariable UUID cartId) {
        String url = restClientConfig.cartServiceUrl + "/carts/" + cartId + "/items";
        return restTemplate.exchange(url, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);
    }

    @Operation(description = "Sets the cart's status to INACTIVE.")
    @DeleteMapping("/{cartId}")
    public ResponseEntity<Void> deleteCart (@PathVariable UUID cartId) {
        String url = restClientConfig.cartServiceUrl + "/carts/" + cartId;
        return restTemplate.exchange(url, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);
    }

    @PutMapping("/items")
    public ResponseEntity<GetCartDTO> updateItemQuantity(@Valid @RequestBody UpdateCartItemDTO updateCartItemDTO) {
        String url = restClientConfig.cartServiceUrl + "/carts/items";
        HttpEntity<UpdateCartItemDTO> requestEntity = new HttpEntity<>(updateCartItemDTO);
        return restTemplate.exchange(url, HttpMethod.PUT, requestEntity, GetCartDTO.class);
    }

}