package io.github.nathanjrussell;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.nathanjrussell.humanresources.ContactInfo;
import io.github.nathanjrussell.humanresources.Employee;
import io.github.nathanjrussell.humanresources.Employees;
import io.github.nathanjrussell.humanresources.NathanAPI;
import io.github.nathanjrussell.humanresources.Role;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class NathanAPITest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static Employee sampleEmployee(String id, String firstName) {
        var c = new ContactInfo(
                firstName,
                "Smith",
                firstName.toLowerCase() + "@example.com",
                "555-0000",
                "1 Main St",
                "Louisville",
                ContactInfo.State.KY,
                "40202"
        );

        return new Employee(
                id,
                c,
                c,
                null,
                Role.IT_LEVEL_1,
                LocalDate.of(2026, 1, 1)
        );
    }

    @Test
    void read_parsesEmployeeListWrapper() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        try {
            server.createContext("/read", exchange -> {
                String responseJson = "{" +
                        "\"pk\":\"uofL_cse_220_api_demo\"," +
                        "\"sk\":\"spring_2026\"," +
                        "\"employee_list\":{" +
                        "\"E-1\":" + sampleEmployee("E-1", "Alice").toJson() + "," +
                        "\"E-2\":" + sampleEmployee("E-2", "Bob").toJson() +
                        "}" +
                        "}";

                byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.close();
            });
            server.start();

            URI endpoint = new URI("http://127.0.0.1:" + server.getAddress().getPort() + "/read");
            Employees employees = new NathanAPI().read(endpoint);

            assertEquals("Alice", employees.lookUp("E-1").employee().firstName());
            assertEquals("Bob", employees.lookUp("E-2").employee().firstName());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void write_sendsEmployeeInfoWrapper() throws Exception {
        AtomicReference<String> receivedBody = new AtomicReference<>();

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        try {
            server.createContext("/write", exchange -> {
                receivedBody.set(readBody(exchange));

                byte[] bytes = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.close();
            });
            server.start();

            URI endpoint = new URI("http://127.0.0.1:" + server.getAddress().getPort() + "/write");
            Employee emp = sampleEmployee("E-9", "Zoe");

            String response = new NathanAPI().write(endpoint, emp);
            assertTrue(response.contains("ok"));

            String body = receivedBody.get();
            assertNotNull(body);

            JsonNode wrapper = MAPPER.readTree(body);
            assertEquals("uofL_cse_220_api_demo", wrapper.get("pk").asText());
            assertEquals("spring_2026", wrapper.get("sk").asText());
            assertTrue(wrapper.get("employee_info").isObject());
            assertEquals("E-9", wrapper.get("employee_info").get("employeeID").asText());
        } finally {
            server.stop(0);
        }
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }
}
