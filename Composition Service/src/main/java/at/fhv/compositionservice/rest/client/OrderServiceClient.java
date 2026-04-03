package at.fhv.compositionservice.rest.client;

import at.fhv.compositionservice.rest.client.dtos.RemoteOrderDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderServiceClient {

    private final RestTemplate restTemplate;
    private static final String ORDER_SERVICE_URL = "http://order-service";

    public RemoteOrderDTO getOrder(String orderId) {
        try {
            String url = ORDER_SERVICE_URL + "/orders/" + orderId;
            log.info("Fetching order from: {}", url);
            return restTemplate.getForObject(url, RemoteOrderDTO.class);
        } catch (Exception e) {
            log.error("Error fetching order {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Failed to fetch order: " + orderId, e);
        }
    }

}