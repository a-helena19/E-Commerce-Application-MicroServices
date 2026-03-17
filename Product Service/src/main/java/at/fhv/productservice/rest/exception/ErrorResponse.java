package at.fhv.productservice.rest.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String title;
    private String message;
    private int status;
    private Map<String, String> fieldErrors;
    private LocalDateTime timestamp;

    public ErrorResponse(String title, String message, int status, Map<String, String> fieldErrors, LocalDateTime timestamp) {
        this.title = title;
        this.message = message;
        this.status = status;
        this.fieldErrors = fieldErrors;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

