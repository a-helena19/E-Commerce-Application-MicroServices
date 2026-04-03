package at.fhv.compositionservice.rest.client;

import at.fhv.compositionservice.rest.client.dtos.RemoteProductDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductServiceClient {

    private final RestTemplate restTemplate;
    private static final String PRODUCT_SERVICE_URL = "http://product-service";

    public RemoteProductDTO getProduct(String productId) {
        try {
            String url = PRODUCT_SERVICE_URL + "/products/" + productId;
            log.info("Fetching product from: {}", url);
            return restTemplate.getForObject(url, RemoteProductDTO.class);
        } catch (Exception e) {
            log.error("Error fetching product {}: {}", productId, e.getMessage());
            throw new RuntimeException("Failed to fetch product: " + productId, e);
        }
    }

}
