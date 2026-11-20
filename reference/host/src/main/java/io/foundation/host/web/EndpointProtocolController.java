package io.foundation.host.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.foundation.host.endpoint.CredentialService;
import io.foundation.host.endpoint.EndpointRecord;
import io.foundation.host.endpoint.EndpointService;
import io.foundation.host.support.Api;
import io.foundation.host.support.Json;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Endpoint protocol v1 (docs/03-endpoint-contract.md). Auth is an endpoint bearer token issued by
 * enroll; all responses use the { code, message, data, traceId } envelope.
 */
@RestController
@RequestMapping("/endpoint/v1")
public class EndpointProtocolController {

    private final EndpointService endpoints;
    private final CredentialService credentials;

    public EndpointProtocolController(EndpointService endpoints, CredentialService credentials) {
        this.endpoints = endpoints;
        this.credentials = credentials;
    }

    @PostMapping("/enroll")
    public ResponseEntity<String> enroll(@RequestBody(required = false) String body) {
        JsonNode request = Json.read(body);
        String token = request.path("token").asText(request.path("enrollToken").asText(""));
        EndpointService.EnrollResult result = endpoints.enroll(token, request);
        return Api.created(Map.of(
                "endpointId", result.endpointId(),
                "endpointToken", result.endpointToken(),
                "desiredRevision", result.desiredRevision()));
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestBody(required = false) String body) {
        EndpointRecord endpoint = endpoints.authenticate(authorization);
        return Api.ok(endpoints.register(endpoint, Json.read(body)));
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<String> heartbeat(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestBody(required = false) String body) {
        EndpointRecord endpoint = endpoints.authenticate(authorization);
        return Api.ok(endpoints.heartbeat(endpoint));
    }

    @GetMapping("/twin")
    public ResponseEntity<String> twin(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestParam(value = "rev", required = false) Integer revision) {
        EndpointRecord endpoint = endpoints.authenticate(authorization);
        EndpointService.TwinView twin = endpoints.twin(endpoint);
        if (revision != null && revision == twin.revision()) {
            return ResponseEntity.status(304)
                    .header(HttpHeaders.ETAG, twin.etag())
                    .build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.ETAG, twin.etag())
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(Api.respond(200, "OK", "ok", twin.desired()).getBody());
    }

    @PostMapping("/twin/report")
    public ResponseEntity<String> reportTwin(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestBody(required = false) String body) {
        EndpointRecord endpoint = endpoints.authenticate(authorization);
        return Api.ok(endpoints.reportTwin(endpoint, Json.read(body)));
    }

    @PostMapping("/report")
    public ResponseEntity<String> report(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody(required = false) String body) {
        EndpointRecord endpoint = endpoints.authenticate(authorization);
        return Api.ok(endpoints.report(endpoint, idempotencyKey, Json.read(body)));
    }

    @GetMapping("/commands")
    public ResponseEntity<String> commands(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        EndpointRecord endpoint = endpoints.authenticate(authorization);
        List<ObjectNode> commands = credentials.pollCommands(endpoint.id());
        return Api.ok(commands);
    }

    @PostMapping("/ack")
    public ResponseEntity<String> ack(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestBody(required = false) String body) {
        EndpointRecord endpoint = endpoints.authenticate(authorization);
        JsonNode request = Json.read(body);
        credentials.ack(endpoint.id(), request.path("commandId").asText(""));
        return Api.ok(Map.of("acked", true));
    }

    @PostMapping("/upgrade/plan")
    public ResponseEntity<String> upgradePlan(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        endpoints.authenticate(authorization);
        return Api.error(501, "NOT_IMPLEMENTED", "upgrade is reserved for v1.1");
    }
}
