package at.fhv.productservice.domain.model.exception;

public class InvalidEventException extends RuntimeException {
    private final String eventType;
    private final String validationError;
    public InvalidEventException(String eventType, String validationError) {
        super(String.format("Invalid event of type '%s': %s", eventType, validationError));
        this.eventType = eventType;
        this.validationError = validationError;
    }

    public String getEventType() {
        return eventType;
    }

    public String getValidationError() {
        return validationError;
    }
}
