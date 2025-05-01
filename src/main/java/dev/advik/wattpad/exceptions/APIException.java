package dev.advik.wattpad.exceptions;

public class APIException extends WattpadException {
    private final Object apiErrorResponse; // Store the raw error response if needed

    public APIException(String message, Object apiErrorResponse) {
        super(message + " (API Response: " + apiErrorResponse + ")");
        this.apiErrorResponse = apiErrorResponse;
    }

    public APIException(String message) {
        super(message);
        this.apiErrorResponse = null;
    }

    public Object getApiErrorResponse() {
        return apiErrorResponse;
    }
}
