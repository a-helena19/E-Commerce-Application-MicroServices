package at.fhv.cartservice.domain.exception;

import java.util.UUID;

public class InvalidCartDataException extends RuntimeException {
    public InvalidCartDataException(String field, UUID value, String reason) {
        super("Invalid cart data for field '" + field + "' with value '" + value + "': " + reason);
    }

    public InvalidCartDataException(String message) {
        super("Invalid cart data: " + message);
    }
}

