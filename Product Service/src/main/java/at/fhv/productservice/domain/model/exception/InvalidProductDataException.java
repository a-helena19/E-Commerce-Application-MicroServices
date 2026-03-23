package at.fhv.productservice.domain.model.exception;

public class InvalidProductDataException extends RuntimeException {
    private final String field;
    private final Object invalidValue;

    public InvalidProductDataException(String field, Object invalidValue, String message) {
        super(String.format("Invalid value for field '%s': %s (%s)", field, invalidValue, message));
        this.field = field;
        this.invalidValue = invalidValue;
    }

    public String getField() {
        return field;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }
}