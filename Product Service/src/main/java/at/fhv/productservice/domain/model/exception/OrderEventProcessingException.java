package at.fhv.productservice.domain.model.exception;

public class OrderEventProcessingException extends RuntimeException {
    private final String eventId;
    private final String details;

    public OrderEventProcessingException(String message) {
        super(message);
        this.eventId = null;
        this.details = null;
    }

    public OrderEventProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.eventId = null;
        this.details = null;
    }

    public OrderEventProcessingException(String message, String eventId, String details, Throwable cause) {
        super(message, cause);
        this.eventId = eventId;
        this.details = details;
    }

    public String getEventId() {
        return eventId;
    }

    public String getDetails() {
        return details;
    }
}
