package at.fhv.springcloudgateway.rest.dtos;

public class HealthResponse {
    public String message;
    public String status;

    public HealthResponse(String message, String status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }
}
