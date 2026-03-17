package at.fhv.api_gateway.rest;

import at.fhv.api_gateway.application.config.RestClientConfig;
import at.fhv.api_gateway.rest.dtos.products.CreateProductDTO;
import at.fhv.api_gateway.rest.dtos.products.GetProductDTO;
import at.fhv.api_gateway.rest.dtos.products.UpdateProductDTO;
import at.fhv.api_gateway.rest.dtos.products.StockUpdateDTO;
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
@RequestMapping("/api/products")
public class ProductRestController {
    private final RestTemplate restTemplate;
    private final RestClientConfig restClientConfig;

    public ProductRestController(RestTemplate restTemplate, RestClientConfig restClientConfig) {
        this.restTemplate = restTemplate;
        this.restClientConfig = restClientConfig;
    }

    @PostMapping
    public ResponseEntity<GetProductDTO> createProduct(@Valid @RequestBody CreateProductDTO dto) {
        String url = restClientConfig.productServiceUrl + "/products";
        HttpEntity<CreateProductDTO> request = new HttpEntity<>(dto);
        return restTemplate.postForEntity(url, request, GetProductDTO.class);
    }

    @GetMapping
    public ResponseEntity<List<GetProductDTO>> getAllProducts() {
        String url = restClientConfig.productServiceUrl + "/products";
        return restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<List<GetProductDTO>>(){});
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetProductDTO> getProductById(@PathVariable UUID id) {
        String url = restClientConfig.productServiceUrl + "/products/" + id;
        return restTemplate.getForEntity(url, GetProductDTO.class);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GetProductDTO> updateProduct(@PathVariable UUID id, @Valid @RequestBody UpdateProductDTO dto) {
        String url = restClientConfig.productServiceUrl + "/products/" + id;
        HttpEntity<UpdateProductDTO> request = new HttpEntity<>(dto);
        return restTemplate.exchange(url, HttpMethod.PUT, request, GetProductDTO.class);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        String url = restClientConfig.productServiceUrl + "/products/" + id;
        restTemplate.exchange(url, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reduce-stock")
    public ResponseEntity<GetProductDTO> reduceStock(@PathVariable UUID id, @Valid @RequestBody StockUpdateDTO dto) {
        String url = restClientConfig.productServiceUrl + "/products/" + id + "/reduce-stock";
        HttpEntity<StockUpdateDTO> request = new HttpEntity<>(dto);
        return restTemplate.postForEntity(url, request, GetProductDTO.class);
    }

    @PostMapping("/{id}/restore-stock")
    public ResponseEntity<GetProductDTO> restoreStock(@PathVariable UUID id, @Valid @RequestBody StockUpdateDTO dto) {
        String url = restClientConfig.productServiceUrl + "/products/" + id + "/restore-stock";
        HttpEntity<StockUpdateDTO> request = new HttpEntity<>(dto);
        return restTemplate.postForEntity(url, request, GetProductDTO.class);
    }
}