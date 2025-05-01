package dev.advik.wattpad.exceptions;

import okhttp3.HttpUrl;

public class NotFoundException extends WattpadException {
    private final String requestedUrl;

    public NotFoundException(HttpUrl url) {
        super("Error 404: The requested resource was not found.");
        this.requestedUrl = url.toString(); // Store the full URL
    }

    public String getRequestedUrl() {
        return requestedUrl;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " URL: " + requestedUrl;
    }
}