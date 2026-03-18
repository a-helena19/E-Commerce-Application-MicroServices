package at.fhv.cartservice.domain.exception;

import java.util.UUID;

public class InvalidCartItemDataException extends RuntimeException {
    public InvalidCartItemDataException(String field, UUID value, String reason) {
        super("Invalid cart item data for field '" + field + "' with value '" + value + "': " + reason);
    }

    public InvalidCartItemDataException(String field, int value, String reason) {
        super("Invalid cart item data for field '" + field + "' with value '" + value + "': " + reason);
    }

    public InvalidCartItemDataException(String message) {
        super(message);
    }
}

