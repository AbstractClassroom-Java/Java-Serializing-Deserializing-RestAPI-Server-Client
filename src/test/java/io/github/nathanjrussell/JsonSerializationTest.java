package io.github.nathanjrussell;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.nathanjrussell.humanresources.ContactInfo;
import io.github.nathanjrussell.humanresources.Employee;
import io.github.nathanjrussell.humanresources.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class JsonSerializationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static ContactInfo contact(String first) {
        return new ContactInfo(
                first,
                "Smith",
                first.toLowerCase() + "@example.com",
                "555-1212",
                "1 Main St",
                "Albany",
                ContactInfo.State.NY,
                "12345-6789"
        );
    }

    @Test
    void contactInfo_roundTripsJson() {
        var c1 = contact("Alice");
        var json = c1.toJson();
        var c2 = ContactInfo.fromJson(json);
        assertEquals(c1, c2);
    }

    @Test
    void role_serializesAsObjectAndParsesBack() throws Exception {
        var json = Role.IT_LEVEL_1.toJson();
        JsonNode node = MAPPER.readTree(json);

        assertTrue(node.isObject());
        assertEquals("IT_LEVEL_1", node.get("name").asText());
        assertEquals(Role.IT_LEVEL_1.title(), node.get("title").asText());
        assertEquals(Role.IT_LEVEL_1.minimumPay(), node.get("minimumPay").asInt());
        assertEquals(Role.IT_LEVEL_1.maximumPay(), node.get("maximumPay").asInt());
        assertEquals(Role.IT_LEVEL_1.description(), node.get("description").asText());

        assertEquals(Role.IT_LEVEL_1, Role.fromJson(json));
        assertEquals(Role.IT_LEVEL_2, Role.fromJson("\"IT_LEVEL_2\""));
    }

    @Test
    void employee_toJsonChainsNestedSerializers() throws Exception {
        var e1 = new Employee(
                "E-2001",
                contact("Emp"),
                contact("Emerg1"),
                null,
                Role.WAREHOUSE_PICKER,
                LocalDate.of(2026, 4, 10)
        );

        var json = e1.toJson();
        JsonNode root = MAPPER.readTree(json);

        assertTrue(root.isObject());
        assertEquals("E-2001", root.get("employeeID").asText());

        // These should be nested objects because Employee.toJson() chains ContactInfo.toJson()
        assertTrue(root.get("employee").isObject());
        assertTrue(root.get("emergencyContact1").isObject());
        assertTrue(root.get("role").isObject());
        assertTrue(root.get("emergencyContact2").isNull());

        assertEquals("WAREHOUSE_PICKER", root.get("role").get("name").asText());

        var e2 = Employee.fromJson(json);
        assertEquals(e1, e2);
    }
}
