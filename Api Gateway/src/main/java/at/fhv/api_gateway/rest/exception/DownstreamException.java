package at.fhv.api_gateway.rest.exception;

public class DownstreamException extends RuntimeException {
    private final ErrorResponseDTO errorResponse;
    private final int httpStatus;

    public DownstreamException(ErrorResponseDTO errorResponse, int httpStatus) {
        super(errorResponse.getMessage());
        this.errorResponse = errorResponse;
        this.httpStatus = httpStatus;
    }

    public ErrorResponseDTO getErrorResponse() {
        return errorResponse;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
