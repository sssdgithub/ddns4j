package io.foundation.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Endpoint protocol test double. Exit codes: 0 success, 1 network/server error, 2 bad arguments,
 * 3 local state error (docs/06-minimal-loop.md A15).
 */
public final class EndpointCli {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final int EXIT_OK = 0;
    private static final int EXIT_HTTP = 1;
    private static final int EXIT_ARGS = 2;
    private static final int EXIT_STATE = 3;

    public static void main(String[] args) {
        int code = new EndpointCli().run(args);
        System.exit(code);
    }

    int run(String[] args) {
        if (args.length == 0) {
            usage();
            return EXIT_ARGS;
        }
        String command = args[0];
        try {
            Args parsed = Args.parse(args);
            return switch (command) {
                case "enroll" -> enroll(parsed);
                case "twin" -> twin(parsed);
                case "report" -> report(parsed);
                case "help", "--help", "-h" -> {
                    usage();
                    yield EXIT_OK;
                }
                default -> {
                    System.err.println("unknown command: " + command);
                    usage();
                    yield EXIT_ARGS;
                }
            };
        } catch (IllegalArgumentException failure) {
            System.err.println("argument error: " + failure.getMessage());
            return EXIT_ARGS;
        } catch (StateException failure) {
            System.err.println("state error: " + failure.getMessage());
            return EXIT_STATE;
        } catch (HttpFailure failure) {
            System.err.println("request failed: " + failure.getMessage());
            return EXIT_HTTP;
        } catch (Exception failure) {
            System.err.println("unexpected failure: " + failure);
            return EXIT_HTTP;
        }
    }

    private int enroll(Args args) throws Exception {
        String server = args.value("server", "http://127.0.0.1:8080");
        String token = args.require("token");
        String name = args.value("name", "cli-endpoint");
        String profile = args.value("profile", "edge");
        Path statePath = Path.of(args.value("state", "endpoint-state.json"));

        ObjectNode enrollBody = JSON.createObjectNode();
        enrollBody.put("token", token);
        enrollBody.put("name", name);
        enrollBody.put("profile", profile);
        ObjectNode platform = enrollBody.putObject("platform");
        platform.put("os", args.value("os", "linux"));
        platform.put("arch", args.value("arch", "arm64"));
        enrollBody.put("version", args.value("version", "0.1.0"));
        var capabilities = enrollBody.putArray("capabilities");
        capabilities.addObject().put("id", "ip.resolve");
        capabilities.addObject().put("id", "dns.update");

        JsonNode enrollData = post(server + "/endpoint/v1/enroll", null, enrollBody.toString());
        String endpointId = text(enrollData, "endpointId");
        String endpointToken = text(enrollData, "endpointToken");

        ObjectNode registerBody = JSON.createObjectNode();
        registerBody.put("profile", profile);
        ObjectNode registerPlatform = registerBody.putObject("platform");
        registerPlatform.put("os", args.value("os", "linux"));
        registerPlatform.put("arch", args.value("arch", "arm64"));
        registerBody.put("version", args.value("version", "0.1.0"));
        var registeredCaps = registerBody.putArray("capabilities");
        registeredCaps.addObject().put("id", "ip.resolve");
        registeredCaps.addObject().put("id", "dns.update");
        post(server + "/endpoint/v1/register", endpointToken, registerBody.toString());
        JsonNode heartbeat = post(server + "/endpoint/v1/heartbeat", endpointToken, "{}");

        ObjectNode state = JSON.createObjectNode();
        state.put("server", server);
        state.put("endpointId", endpointId);
        state.put("endpointToken", endpointToken);
        state.put("profile", profile);
        state.put("desiredRevision", heartbeat.path("desiredRevision").asInt(1));
        state.put("reportedRevision", 0);
        save(statePath, state);

        System.out.println("enrolled endpointId=" + endpointId
                + " desiredRevision=" + heartbeat.path("desiredRevision").asInt(1)
                + " state=" + statePath.toAbsolutePath());
        return EXIT_OK;
    }

    private int twin(Args args) throws Exception {
        ObjectNode state = load(Path.of(args.value("state", "endpoint-state.json")));
        String server = args.value("server", state.path("server").asText("http://127.0.0.1:8080"));
        HttpResponse<String> response = get(server + "/endpoint/v1/twin", state.path("endpointToken").asText());
        if (response.statusCode() == 304) {
            System.out.println("twin up-to-date revision=" + state.path("reportedRevision").asInt());
            return EXIT_OK;
        }
        JsonNode data = envelope(response);
        int revision = data.path("revision").asInt();
        state.put("desiredRevision", revision);
        save(Path.of(args.value("state", "endpoint-state.json")), state);
        System.out.println(JSON.writerWithDefaultPrettyPrinter().writeValueAsString(data));
        return EXIT_OK;
    }

    private int report(Args args) throws Exception {
        Path statePath = Path.of(args.value("state", "endpoint-state.json"));
        ObjectNode state = load(statePath);
        String server = args.value("server", state.path("server").asText("http://127.0.0.1:8080"));
        String token = state.path("endpointToken").asText();

        JsonNode desired = envelope(get(server + "/endpoint/v1/twin", token));
        int desiredRevision = desired.path("revision").asInt(1);
        int reportedRevision = desiredRevision;

        ObjectNode twinReport = JSON.createObjectNode();
        if (args.has("fail")) {
            reportedRevision = state.path("reportedRevision").asInt(0);
            twinReport.put("revision", reportedRevision);
            twinReport.set("config", desired.path("config"));
            twinReport.set("modules", desired.path("modules"));
            twinReport.put("appliedAt", Instant.now().toString());
            twinReport.put("state", "error");
            twinReport.put("lastError", args.value("fail", "report blocked by operator"));
        } else {
            twinReport.put("revision", desiredRevision);
            twinReport.set("config", desired.path("config"));
            twinReport.set("modules", desired.path("modules"));
            twinReport.put("appliedAt", Instant.now().toString());
            twinReport.put("state", "ok");
            twinReport.putNull("lastError");
        }
        post(server + "/endpoint/v1/twin/report", token, twinReport.toString());

        ObjectNode report = JSON.createObjectNode();
        report.put("endpointId", state.path("endpointId").asText());
        report.put("reportedAt", Instant.now().toString());
        report.putObject("health").put("state", "ok").put("uptimeSec", 1);
        report.putObject("metrics").put("cpu", 0.01).put("memMb", 24);
        report.putArray("results");
        String idempotencyKey = args.value("idempotency-key", "cli-" + System.currentTimeMillis());
        post(server + "/endpoint/v1/report", token, report.toString(), idempotencyKey);

        state.put("reportedRevision", reportedRevision);
        state.put("desiredRevision", desiredRevision);
        save(statePath, state);
        System.out.println("reported revision=" + reportedRevision + " desiredRevision=" + desiredRevision
                + (args.has("fail") ? " state=error" : " state=ok"));
        return EXIT_OK;
    }

    private JsonNode post(String url, String endpointToken, String body) throws Exception {
        return post(url, endpointToken, body, null);
    }

    private JsonNode post(String url, String endpointToken, String body, String idempotencyKey)
            throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (endpointToken != null) {
            builder.header("Authorization", "Bearer " + endpointToken);
        }
        if (idempotencyKey != null) {
            builder.header("Idempotency-Key", idempotencyKey);
        }
        return envelope(client().send(builder.build(), HttpResponse.BodyHandlers.ofString()));
    }

    private HttpResponse<String> get(String url, String endpointToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .header("Authorization", "Bearer " + endpointToken)
                .build();
        return client().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpClient client() {
        return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    private static JsonNode envelope(HttpResponse<String> response) {
        if (response.statusCode() == 304) {
            return JSON.createObjectNode();
        }
        JsonNode body;
        try {
            body = JSON.readTree(response.body());
        } catch (IOException failure) {
            throw new HttpFailure("HTTP " + response.statusCode() + " with non-JSON body");
        }
        if (response.statusCode() >= 400) {
            throw new HttpFailure("HTTP " + response.statusCode() + " code=" + body.path("code").asText()
                    + " message=" + body.path("message").asText());
        }
        return body.path("data");
    }

    private static String text(JsonNode node, String field) {
        String value = node.path(field).asText("");
        if (value.isBlank()) {
            throw new HttpFailure("response misses " + field);
        }
        return value;
    }

    private static ObjectNode load(Path path) {
        if (!Files.exists(path)) {
            throw new StateException("state file " + path.toAbsolutePath() + " does not exist, run enroll first");
        }
        try {
            return (ObjectNode) JSON.readTree(path.toFile());
        } catch (IOException failure) {
            throw new StateException("cannot parse state file " + path + ": " + failure.getMessage());
        }
    }

    private static void save(Path path, ObjectNode state) {
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            JSON.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), state);
        } catch (IOException failure) {
            throw new StateException("cannot write state file " + path + ": " + failure.getMessage());
        }
    }

    private static void usage() {
        System.out.println("""
                Foundation endpoint CLI (protocol test double)

                Usage:
                  endpoint-cli enroll --token <enroll-token> [--server URL] [--name NAME] [--state FILE]
                  endpoint-cli twin   [--server URL] [--state FILE]
                  endpoint-cli report [--server URL] [--state FILE] [--idempotency-key KEY] [--fail REASON]

                Exit codes: 0 ok, 1 network/server error, 2 argument error, 3 local state error
                """);
    }

    static final class Args {
        private final Map<String, String> values = new LinkedHashMap<>();
        private final Set<String> flags = new java.util.HashSet<>();

        static Args parse(String[] args) {
            Args parsed = new Args();
            for (int i = 1; i < args.length; i++) {
                String arg = args[i];
                if (!arg.startsWith("--")) {
                    throw new IllegalArgumentException("unexpected positional argument: " + arg);
                }
                String key = arg.substring(2);
                if (key.isBlank()) {
                    throw new IllegalArgumentException("empty option name");
                }
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    parsed.values.put(key, args[++i]);
                } else {
                    parsed.flags.add(key);
                }
            }
            return parsed;
        }

        String value(String key, String fallback) {
            return values.getOrDefault(key, fallback);
        }

        String require(String key) {
            String value = values.get(key);
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("--" + key + " is required");
            }
            return value;
        }

        boolean has(String key) {
            return flags.contains(key) || values.containsKey(key);
        }
    }

    static final class StateException extends RuntimeException {
        StateException(String message) {
            super(message);
        }
    }

    static final class HttpFailure extends RuntimeException {
        HttpFailure(String message) {
            super(message.toLowerCase(Locale.ROOT));
        }
    }
}
