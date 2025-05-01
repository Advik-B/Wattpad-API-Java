package dev.advik.wattpad.exceptions;

public class WattpadException extends RuntimeException {
    public WattpadException(String message) {
        super(message);
    }

    public WattpadException(String message, Throwable cause) {
        super(message, cause);
    }
}
