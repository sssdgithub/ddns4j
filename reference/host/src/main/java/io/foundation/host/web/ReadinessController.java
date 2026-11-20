package io.foundation.host.web;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReadinessController {

    private final long startedAt = System.currentTimeMillis();

    @GetMapping("/readyz")
    public ResponseEntity<String> ready() {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body("ready uptimeMs=" + (System.currentTimeMillis() - startedAt));
    }
}
