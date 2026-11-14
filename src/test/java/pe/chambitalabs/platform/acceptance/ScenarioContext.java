package pe.chambitalabs.platform.acceptance;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.spring.ScenarioScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/** State of one scenario: actors and their tokens, saved ids and the last HTTP response. */
@Component
@ScenarioScope
public class ScenarioContext {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\w+)}");

    private final RestClient client;
    private final String suffix = UUID.randomUUID().toString().substring(0, 8);
    private final Map<String, Actor> actors = new HashMap<>();
    private final Map<String, String> values = new HashMap<>();
    private int lastStatus;
    private String lastBody;

    public ScenarioContext(@Value("${local.server.port}") int port) {
        this.client = RestClient.builder().baseUrl("http://localhost:" + port)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> { })
                .build();
        values.put("RunId", suffix);
    }

    public String emailOf(String name, String domain) {
        return name.toLowerCase() + "." + suffix + "@" + domain;
    }

    public void addActor(String name, Actor actor) {
        actors.put(name, actor);
    }

    public Actor actor(String name) {
        var actor = actors.get(name);
        if (actor == null) {
            throw new IllegalStateException("Unknown actor " + name);
        }
        return actor;
    }

    public void put(String key, String value) {
        values.put(key, value);
    }

    public String get(String key) {
        var value = values.get(key);
        if (value == null) {
            throw new IllegalStateException("No value saved for " + key);
        }
        return value;
    }

    public String resolve(String text) {
        var matcher = PLACEHOLDER.matcher(text);
        var result = new StringBuilder();
        while (matcher.find()) {
            var key = matcher.group(1);
            var replacement = values.containsKey(key) ? values.get(key)
                    : actors.containsKey(key) ? actors.get(key).id() : matcher.group();
            matcher.appendReplacement(result, java.util.regex.Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public void send(String method, String path, String body, String token, String language) {
        var spec = client.method(org.springframework.http.HttpMethod.valueOf(method)).uri(resolve(path));
        if (token != null) {
            spec = spec.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        if (language != null) {
            spec = spec.header(HttpHeaders.ACCEPT_LANGUAGE, language);
        }
        if (body != null) {
            spec = spec.contentType(MediaType.APPLICATION_JSON).body(resolve(body));
        }
        spec.exchange((request, response) -> {
            lastStatus = response.getStatusCode().value();
            lastBody = new String(response.getBody().readAllBytes());
            return null;
        });
    }

    public int lastStatus() {
        return lastStatus;
    }

    public String lastBody() {
        return lastBody;
    }

    public <T> T read(String jsonPath) {
        return JsonPath.read(lastBody, jsonPath);
    }

    /** A signed-in user of a scenario. */
    public record Actor(String id, String email, String password, String token) {
    }
}
