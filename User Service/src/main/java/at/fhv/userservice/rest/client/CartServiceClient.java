package at.fhv.userservice.rest.client;

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

    public UUID createCartForUser(UUID userId) {
        String url = CART_SERVICE_NAME + "/carts";
        CreateCartRequestDTO request = new CreateCartRequestDTO(userId);

        try {
            var response = restTemplate.postForEntity(url, request, CreateCartResponseDTO.class);
            if (response.getBody() != null) {
                return response.getBody().getId();
            }
            throw new RuntimeException("No response body from Cart Service");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create cart in Cart Service: " + e.getMessage(), e);
        }
    }

    public UUID getCartByUserId(UUID userId) {
        String url = CART_SERVICE_NAME + "/carts/user/" + userId;

        try {
            var response = restTemplate.getForEntity(url, CreateCartResponseDTO.class);
            if (response.getBody() != null) {
                return response.getBody().getId();
            }
            throw new RuntimeException("No response body from Cart Service");
        } catch (Exception e) {
            return null;
        }
    }

    public void deleteCartByUserId(UUID userId) {
        UUID cartId = getCartByUserId(userId);
        if (cartId != null) {
            deleteCartById(cartId);
        }
    }

    public void deleteCartById(UUID cartId) {
        String url = CART_SERVICE_NAME + "/carts/" + cartId;

        try {
            restTemplate.delete(url);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete cart in Cart Service: " + e.getMessage(), e);
        }
    }
}

