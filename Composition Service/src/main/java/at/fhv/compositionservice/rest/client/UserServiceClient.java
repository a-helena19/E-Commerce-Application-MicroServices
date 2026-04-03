package at.fhv.compositionservice.rest.client;

import at.fhv.compositionservice.rest.client.dtos.RemoteUserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestTemplate restTemplate;
    private static final String USER_SERVICE_URL = "http://user-service";

    public RemoteUserDTO getUser(String userId) {
        try {
            String url = USER_SERVICE_URL + "/users/" + userId;
            log.info("Fetching user from: {}", url);
            return restTemplate.getForObject(url, RemoteUserDTO.class);
        } catch (Exception e) {
            log.error("Error fetching user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to fetch user: " + userId, e);
        }
    }

}