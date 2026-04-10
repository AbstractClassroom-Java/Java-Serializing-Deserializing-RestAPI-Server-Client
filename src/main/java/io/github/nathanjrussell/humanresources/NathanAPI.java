package io.github.nathanjrussell.humanresources;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

// Simple JSON client for the demo REST API.
public final class NathanAPI {

    private static final String PK = "uofL_cse_220_api_demo";
    private static final String SK = "spring_2026";

    private final HttpClient client;

    public NathanAPI() {
        this(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build());
    }

    public NathanAPI(HttpClient client) {
        this.client = Objects.requireNonNull(client, "client");
    }

    // GET wrapper {pk, sk, employee_list}.
    public Employees read(URI endpoint) throws IOException, InterruptedException {
        Objects.requireNonNull(endpoint, "endpoint");

        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .timeout(Duration.ofSeconds(20))
                .GET()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IOException("GET " + endpoint + " failed with status " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = JsonUtil.readValue(response.body(), JsonNode.class);
        if (root == null || !root.isObject()) {
            throw new IOException("API response was not a JSON object");
        }

        var pkNode = root.get("pk");
        var skNode = root.get("sk");
        if (pkNode != null && pkNode.isTextual() && !PK.equals(pkNode.asText())) {
            throw new IOException("Unexpected pk: " + pkNode.asText());
        }
        if (skNode != null && skNode.isTextual() && !SK.equals(skNode.asText())) {
            throw new IOException("Unexpected sk: " + skNode.asText());
        }

        JsonNode listNode = root.get("employee_list");
        if (listNode == null || listNode.isNull()) {
            throw new IOException("Missing employee_list in API response");
        }

        Employees employees = new Employees();

        if (listNode.isObject()) {
            listNode.fields().forEachRemaining(e -> {
                Employee emp = Employee.fromJson(e.getValue().toString());
                employees.add(emp);
            });
            return employees;
        }

        if (listNode.isArray()) {
            for (JsonNode empNode : listNode) {
                Employee emp = Employee.fromJson(empNode.toString());
                employees.add(emp);
            }
            return employees;
        }

        throw new IOException("employee_list must be an object or array");
    }

    // POST wrapper {pk, sk, employee_info}.
    public String write(URI endpoint, Employee employee) throws IOException, InterruptedException {
        Objects.requireNonNull(endpoint, "endpoint");
        Objects.requireNonNull(employee, "employee");

        var root = JsonUtil.MAPPER.createObjectNode();
        root.put("pk", PK);
        root.put("sk", SK);
        root.set("employee_info", JsonUtil.readValue(employee.toJson(), JsonNode.class));

        String body = JsonUtil.writeValueAsString(root);

        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IOException("POST " + endpoint + " failed with status " + response.statusCode() + ": " + response.body());
        }

        return response.body();
    }
}
