package at.fhv.orderservice.rest.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class CartServiceClient {
    private static final String CART_SERVICE_NAME = "http://cart-service";
    private final RestTemplate restTemplate;

    public CartServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GetCartResponseDTO getCartByUserId(UUID userId) {
        String url = CART_SERVICE_NAME + "/carts/user/" + userId;

        try {
            var response = restTemplate.getForEntity(url, GetCartResponseDTO.class);
            if (response.getBody() != null) {
                return response.getBody();
            }
            throw new RuntimeException("No response body from Cart Service");
        } catch (Exception e) {
            throw new RuntimeException("Failed to get cart from Cart Service: " + e.getMessage(), e);
        }
    }

    public void clearCartByUserId(UUID userId) {
        GetCartResponseDTO cart = getCartByUserId(userId);
        if (cart != null) {
            clearCartById(cart.getCartId());
        }
    }

    public void clearCartById(UUID cartId) {
        String url = CART_SERVICE_NAME + "/carts/" + cartId + "/items";

        try {
            restTemplate.delete(url);
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear cart in Cart Service: " + e.getMessage(), e);
        }
    }
}

