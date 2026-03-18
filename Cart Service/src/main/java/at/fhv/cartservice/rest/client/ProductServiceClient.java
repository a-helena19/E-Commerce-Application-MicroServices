package at.fhv.cartservice.rest.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class ProductServiceClient {
    private final RestTemplate restTemplate;
    private final String productServiceUrl;

    public ProductServiceClient(RestTemplate restTemplate,
                               @Value("${product.service.url}") String productServiceUrl) {
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
    }

    public GetProductResponseDTO getProductById(UUID productId) {
        String url = productServiceUrl + "/products/" + productId;

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
}

