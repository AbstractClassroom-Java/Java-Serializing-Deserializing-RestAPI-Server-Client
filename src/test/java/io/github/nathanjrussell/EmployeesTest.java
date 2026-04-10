package io.github.nathanjrussell;

import io.github.nathanjrussell.humanresources.ContactInfo;
import io.github.nathanjrussell.humanresources.Employee;
import io.github.nathanjrussell.humanresources.Employees;
import io.github.nathanjrussell.humanresources.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class EmployeesTest {

    private static ContactInfo contact(String first) {
        return new ContactInfo(
                first,
                "Smith",
                first.toLowerCase() + "@example.com",
                "555-3333",
                "1 Main St",
                "Albany",
                ContactInfo.State.NY,
                "12345"
        );
    }

    private static Employee employee(String id, String name) {
        return new Employee(
                id,
                contact(name),
                contact("Emergency" + name),
                null,
                Role.IT_LEVEL_1,
                LocalDate.of(2026, 1, 1)
        );
    }

    @Test
    void addAndLookUpById() {
        var employees = new Employees();
        var e = employee("E-3001", "Alice");

        assertNull(employees.lookUp("E-3001"));
        employees.add(e);

        assertEquals(e, employees.lookUp("E-3001"));
        assertNull(employees.lookUp("DOES_NOT_EXIST"));
        assertNull(employees.lookUp(null));
    }

    @Test
    void getIDs_returnsAllIds() {
        var employees = new Employees();
        employees.add(employee("E-3001", "Alice"));
        employees.add(employee("E-3002", "Bob"));

        var ids = employees.getIDs();
        Arrays.sort(ids);

        assertArrayEquals(new String[]{"E-3001", "E-3002"}, ids);
    }

    @Test
    void jsonRoundTrip() {
        var employees = new Employees();
        employees.add(employee("E-3001", "Alice"));
        employees.add(employee("E-3002", "Bob"));

        var json = employees.toJson();
        var parsed = Employees.fromJson(json);

        assertEquals("Alice", parsed.lookUp("E-3001").employee().firstName());
        assertEquals("Bob", parsed.lookUp("E-3002").employee().firstName());
    }
}
