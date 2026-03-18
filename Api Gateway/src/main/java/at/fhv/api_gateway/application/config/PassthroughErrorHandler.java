package at.fhv.api_gateway.application.config;

import at.fhv.api_gateway.rest.exception.DownstreamException;
import at.fhv.api_gateway.rest.exception.ErrorResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class PassthroughErrorHandler implements ResponseErrorHandler {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isError();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        int statusCode = response.getStatusCode().value();
        String rawBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
        ErrorResponseDTO errorResponse = tryDeserialize(rawBody, statusCode);
        throw new DownstreamException(errorResponse, statusCode);
    }

    private ErrorResponseDTO tryDeserialize(String rawBody, int statusCode) {
        try {
            JsonNode json = objectMapper.readTree(rawBody);
            ErrorResponseDTO errorResponse = new ErrorResponseDTO();
            errorResponse.setStatus(statusCode);
            errorResponse.setTimestamp(LocalDateTime.now());

            if (json.has("message")) {
                errorResponse.setMessage(json.get("message").asText());
            }

            if (json.has("title")) {
                errorResponse.setTitle(json.get("title").asText());
            } else if (json.has("error")) {
                errorResponse.setTitle(json.get("error").asText());
            }

            if (json.has("fieldErrors")) {
                errorResponse.setFieldErrors(objectMapper.convertValue(json.get("fieldErrors"),
                        objectMapper.getTypeFactory().constructMapType(java.util.Map.class, String.class, String.class))
                );
            }
            return errorResponse;

        } catch (Exception e) {
            ErrorResponseDTO fallback = new ErrorResponseDTO();
            fallback.setStatus(statusCode);
            fallback.setTitle("Upstream Error");
            fallback.setMessage(rawBody.isBlank() ? "An error occurred in a downstream service." : rawBody);
            fallback.setTimestamp(LocalDateTime.now());
            return fallback;
        }
    }
}
