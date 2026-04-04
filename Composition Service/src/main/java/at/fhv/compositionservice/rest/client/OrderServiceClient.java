package at.fhv.compositionservice.rest.client;

import at.fhv.compositionservice.rest.client.dtos.RemoteOrderDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderServiceClient {

    private final WebClient webClient;
    private static final String ORDER_SERVICE_URL = "http://order-service";

    public Mono<RemoteOrderDTO> getOrder(String orderId) {
        String url = ORDER_SERVICE_URL + "/orders/" + orderId;
        log.info("Fetching order from: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(RemoteOrderDTO.class)
                .doOnError(e -> log.error("Error fetching order {}: {}", orderId, e.getMessage()))
                .onErrorMap(e -> new RuntimeException("Failed to fetch order: " + orderId, e));
    }
}