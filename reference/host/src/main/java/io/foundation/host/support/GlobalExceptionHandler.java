package io.foundation.host.support;

import io.foundation.api.FoundationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<String> handleApi(ApiException failure) {
        return Api.error(failure.status(), failure.code(), failure.getMessage());
    }

    @ExceptionHandler(FoundationException.class)
    public ResponseEntity<String> handleFoundation(FoundationException failure) {
        int status = switch (failure.getCode()) {
            case DEPENDENCY_MISSING -> 404;
            case SERVICE_CONFLICT -> 409;
            case MODULE_INACTIVE -> 503;
            default -> 400;
        };
        return Api.error(status, failure.getCode().name(), failure.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException failure) {
        return Api.error(400, "BAD_REQUEST", failure.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleOther(Exception failure) {
        log.error("unhandled request failure", failure);
        return Api.error(500, "INTERNAL_ERROR", failure.getClass().getSimpleName() + ": " + failure.getMessage());
    }
}
