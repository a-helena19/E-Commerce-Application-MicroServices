package at.fhv.compositionservice.rest.client;

import at.fhv.compositionservice.rest.client.dtos.RemoteProductDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductServiceClient {

    private final WebClient webClient;
    private static final String PRODUCT_SERVICE_URL = "http://product-service";

    public Mono<RemoteProductDTO> getProduct(String productId) {
        String url = PRODUCT_SERVICE_URL + "/products/" + productId;
        log.debug("Fetching product from: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(RemoteProductDTO.class)
                .doOnError(e -> log.error("Error fetching product {}: {}", productId, e.getMessage()))
                .onErrorMap(e -> new RuntimeException("Failed to fetch product: " + productId, e));
    }
}