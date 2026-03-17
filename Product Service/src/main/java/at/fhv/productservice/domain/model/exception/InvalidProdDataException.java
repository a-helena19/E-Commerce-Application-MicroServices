package at.fhv.productservice.domain.model.exception;

public class InvalidProdDataException extends RuntimeException {
    private final String field;
    private final Object invalidValue;

    public InvalidProdDataException(String field, Object invalidValue, String message) {
        super(message);
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