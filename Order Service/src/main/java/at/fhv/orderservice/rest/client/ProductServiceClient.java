package at.fhv.orderservice.rest.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class ProductServiceClient {
    private static final String PRODUCT_SERVICE_NAME = "http://product-service";
    private final RestTemplate restTemplate;

    public ProductServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GetProductResponseDTO getProductById(UUID productId) {
        String url = PRODUCT_SERVICE_NAME + "/products/" + productId;

        try {
            var response = restTemplate.getForEntity(url, GetProductResponseDTO.class);
            if (response.getBody() != null) {
                return response.getBody();
            }
            throw new RuntimeException("No response body from Product Service");
        } catch (Exception e) {
            throw new RuntimeException("Failed to get product from Product Service: " + e.getMessage(), e);
        }
    }

    public GetProductResponseDTO reduceStock(UUID productId, int quantity) {
        String url = PRODUCT_SERVICE_NAME + "/products/" + productId + "/reduce-stock";
        StockUpdateRequestDTO request = new StockUpdateRequestDTO(quantity);

        try {
            var response = restTemplate.postForEntity(url, request, GetProductResponseDTO.class);
            if (response.getBody() != null) {
                return response.getBody();
            }
            throw new RuntimeException("No response body from Product Service");
        } catch (Exception e) {
            throw new RuntimeException("Failed to reduce stock in Product Service: " + e.getMessage(), e);
        }
    }

    public GetProductResponseDTO restoreStock(UUID productId, int quantity) {
        String url = PRODUCT_SERVICE_NAME + "/products/" + productId + "/restore-stock";
        StockUpdateRequestDTO request = new StockUpdateRequestDTO(quantity);

        try {
            var response = restTemplate.postForEntity(url, request, GetProductResponseDTO.class);
            if (response.getBody() != null) {
                return response.getBody();
            }
            throw new RuntimeException("No response body from Product Service");
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore stock in Product Service: " + e.getMessage(), e);
        }
    }
}

