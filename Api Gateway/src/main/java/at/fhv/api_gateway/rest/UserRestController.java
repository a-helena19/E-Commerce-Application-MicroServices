package at.fhv.api_gateway.rest;

import at.fhv.api_gateway.application.config.RestClientConfig;
import at.fhv.api_gateway.rest.dtos.user.CreateUserDTO;
import at.fhv.api_gateway.rest.dtos.user.GetUserDTO;
import at.fhv.api_gateway.rest.dtos.user.UpdateUserDTO;
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
@RequestMapping("/api/users")
public class UserRestController {

    private final RestTemplate restTemplate;
    private final RestClientConfig restClientConfig;

    public UserRestController(RestTemplate restTemplate, RestClientConfig restClientConfig) {
        this.restTemplate = restTemplate;
        this.restClientConfig = restClientConfig;
    }
    @GetMapping
    public ResponseEntity<List<GetUserDTO>> getAllUsers() {
        String url = restClientConfig.userServiceUrl + "/users";
        return restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<List<GetUserDTO>>(){});
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetUserDTO> getUser(@PathVariable UUID id) {
        String url = restClientConfig.userServiceUrl + "/users/" + id;
        return restTemplate.getForEntity(url, GetUserDTO.class);
    }

    @PostMapping
    public ResponseEntity<GetUserDTO> createUser(@Valid @RequestBody CreateUserDTO user) {
        String url = restClientConfig.userServiceUrl + "/users";
        HttpEntity<CreateUserDTO> request = new HttpEntity<>(user);
        return restTemplate.postForEntity(url, request, GetUserDTO.class);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GetUserDTO> updateUser(@PathVariable UUID id, @Valid @RequestBody UpdateUserDTO userDTO) {
        String url = restClientConfig.userServiceUrl + "/users/" + id;
        HttpEntity<UpdateUserDTO> request = new HttpEntity<>(userDTO);
        return restTemplate.exchange(url, HttpMethod.PUT, request, GetUserDTO.class);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        String url = restClientConfig.userServiceUrl + "/users/" + id;
        restTemplate.exchange(url, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);
        return ResponseEntity.noContent().build();
    }

}