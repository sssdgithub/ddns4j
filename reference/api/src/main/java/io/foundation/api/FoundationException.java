package io.foundation.api;

public class FoundationException extends RuntimeException {

    private final ErrorCode code;

    public FoundationException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public FoundationException(ErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }
}
