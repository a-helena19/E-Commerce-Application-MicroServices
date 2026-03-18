package at.fhv.api_gateway.rest;

import at.fhv.api_gateway.application.config.RestClientConfig;
import at.fhv.api_gateway.rest.dtos.order.CreateOrderDTO;
import at.fhv.api_gateway.rest.dtos.order.GetOrderDTO;
import jakarta.validation.Valid;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderRestController {
    private final RestTemplate restTemplate;
    private final RestClientConfig restClientConfig;

    public OrderRestController(RestTemplate restTemplate, RestClientConfig restClientConfig) {
        this.restTemplate = restTemplate;
        this.restClientConfig = restClientConfig;
    }

    @GetMapping
    public ResponseEntity<List<GetOrderDTO>> getAllOrders() {
        String url = restClientConfig.orderServiceUrl + "/orders";
        return restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<List<GetOrderDTO>>(){});
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetOrderDTO> getOrder(@PathVariable UUID id) {
        String url = restClientConfig.orderServiceUrl + "/orders/" + id;
        return restTemplate.getForEntity(url, GetOrderDTO.class);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GetOrderDTO>> getOrdersByUserId(@PathVariable UUID userId) {
        String url = restClientConfig.orderServiceUrl + "/orders/user/" + userId;
        return restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<List<GetOrderDTO>>(){});
    }

    @PostMapping
    public ResponseEntity<GetOrderDTO> createOrder(@Valid @RequestBody CreateOrderDTO dto) {
        String url = restClientConfig.orderServiceUrl + "/orders";
        HttpEntity<CreateOrderDTO> request = new HttpEntity<>(dto);
        return restTemplate.postForEntity(url, request, GetOrderDTO.class);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable UUID id) {
        String url = restClientConfig.orderServiceUrl + "/orders/" + id;
        restTemplate.exchange(url, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);
        return ResponseEntity.noContent().build();

    }

}
