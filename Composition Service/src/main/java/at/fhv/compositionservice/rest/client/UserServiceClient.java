package at.fhv.compositionservice.rest.client;

import at.fhv.compositionservice.rest.client.dtos.RemoteUserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final WebClient webClient;
    private static final String USER_SERVICE_URL = "http://user-service";

    public Mono<RemoteUserDTO> getUser(String userId) {
        String url = USER_SERVICE_URL + "/users/" + userId;
        log.info("Fetching user from: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(RemoteUserDTO.class)
                .doOnError(e -> log.error("Error fetching user {}: {}", userId, e.getMessage()))
                .onErrorMap(e -> new RuntimeException("Failed to fetch user: " + userId, e));
    }
}