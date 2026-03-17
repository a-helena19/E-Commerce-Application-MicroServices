package at.fhv.userservice.rest.client;

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

    public UUID createCartForUser(UUID userId) {
        String url = cartServiceUrl + "/carts";
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
        String url = cartServiceUrl + "/carts/user/" + userId;

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
}

