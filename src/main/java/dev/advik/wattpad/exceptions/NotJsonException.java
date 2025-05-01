package dev.advik.wattpad.exceptions;

public class NotJsonException extends WattpadException {
    private final String responseBody;

    public NotJsonException(String message, String responseBody, Throwable cause) {
        super(message, cause);
        this.responseBody = responseBody;
    }

    public NotJsonException(String message, Throwable cause) {
        super(message, cause);
        this.responseBody = "<Response body not available or too large>";
    }

    public String getResponseBody() {
        return responseBody;
    }

    @Override
    public String getMessage() {
        // Truncate long bodies if needed
        String truncatedBody = responseBody.length() > 500 ? responseBody.substring(0, 500) + "..." : responseBody;
        return super.getMessage() + "\nResponse Body (truncated): " + truncatedBody;
    }
}