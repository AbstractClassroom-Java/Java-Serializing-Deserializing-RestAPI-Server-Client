package io.github.nathanjrussell.restserver;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.nathanjrussell.humanresources.Employee;
import io.github.nathanjrussell.humanresources.Employees;
import io.github.nathanjrussell.humanresources.JsonUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.Executors;

// Local demo REST server.
public final class RestServerMain {

    private static final String PK = "uofL_cse_220_api_demo";
    private static final String SK = "spring_2026";

    private static final Employees EMPLOYEES = new Employees();

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length >= 1 && !args[0].isBlank()) {
            port = Integer.parseInt(args[0]);
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(4));

        server.createContext("/employee", RestServerMain::handleEmployee);
        server.createContext("/employees", RestServerMain::handleEmployees);

        server.start();
        System.out.println("REST server started on http://0.0.0.0:" + port);
        System.out.println("POST  http://<server-ip>:" + port + "/employee");
        System.out.println("GET   http://<server-ip>:" + port + "/employees");
    }

    private static void handleEmployee(HttpExchange exchange) throws IOException {
        logRequest(exchange);
        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonNode root = JsonUtil.readValue(body, JsonNode.class);
            if (root == null || !root.isObject()) {
                sendJson(exchange, 400, "{\"error\":\"Expected JSON object\"}");
                return;
            }

            if (!PK.equals(text(root.get("pk"))) || !SK.equals(text(root.get("sk")))) {
                sendJson(exchange, 400, "{\"error\":\"Invalid pk/sk\"}");
                return;
            }

            JsonNode info = root.get("employee_info");
            if (info == null || info.isNull() || !info.isObject()) {
                sendJson(exchange, 400, "{\"error\":\"Missing employee_info\"}");
                return;
            }

            Employee employee = Employee.fromJson(info.toString());
            EMPLOYEES.add(employee);

            var response = JsonUtil.MAPPER.createObjectNode();
            response.put("ok", true);
            response.put("storedEmployeeID", employee.employeeID());
            response.put("storedCount", EMPLOYEES.getIDs().length);
            response.put("timestamp", Instant.now().toString());

            sendJson(exchange, 200, JsonUtil.writeValueAsString(response));
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, "{\"error\":\"" + escape(e.getMessage()) + "\"}");
        } catch (Exception e) {
            sendJson(exchange, 500, "{\"error\":\"Internal Server Error\"}");
        }
    }

    private static void handleEmployees(HttpExchange exchange) throws IOException {
        logRequest(exchange);
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            var root = JsonUtil.MAPPER.createObjectNode();
            root.put("pk", PK);
            root.put("sk", SK);

            JsonNode employeesRoot = JsonUtil.readValue(EMPLOYEES.toJson(), JsonNode.class);
            JsonNode employeesMap = employeesRoot.get("employees");
            root.set("employee_list", Objects.requireNonNullElseGet(employeesMap, () -> JsonUtil.MAPPER.createObjectNode()));

            sendJson(exchange, 200, JsonUtil.writeValueAsString(root));
        } catch (Exception e) {
            sendJson(exchange, 500, "{\"error\":\"Internal Server Error\"}");
        }
    }

    private static void logRequest(HttpExchange exchange) {
        String clientIp = exchange.getRemoteAddress() != null && exchange.getRemoteAddress().getAddress() != null
                ? exchange.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI() != null ? exchange.getRequestURI().getPath() : "";
        System.out.println("[" + clientIp + "] " + method + " " + path);
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String text(JsonNode node) {
        return node != null && node.isTextual() ? node.asText() : null;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
