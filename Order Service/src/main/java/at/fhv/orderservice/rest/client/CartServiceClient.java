package at.fhv.orderservice.rest.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class CartServiceClient {
    private final RestTemplate restTemplate;
    private final String cartServiceUrl;

    public CartServiceClient(RestTemplate restTemplate,
                           @Value("${cart.service.url}") String cartServiceUrl) {
        this.restTemplate = restTemplate;
        this.cartServiceUrl = cartServiceUrl;
    }

    public GetCartResponseDTO getCartByUserId(UUID userId) {
        String url = cartServiceUrl + "/carts/user/" + userId;

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
        String url = cartServiceUrl + "/carts/" + cartId + "/items";

        try {
            restTemplate.delete(url);
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear cart in Cart Service: " + e.getMessage(), e);
        }
    }
}

