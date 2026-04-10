package io.github.nathanjrussell;

import io.github.nathanjrussell.humanresources.ContactInfo;
import io.github.nathanjrussell.humanresources.Employee;
import io.github.nathanjrussell.humanresources.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    private static ContactInfo contact(String first) {
        return new ContactInfo(
                first,
                "Smith",
                first.toLowerCase() + "@example.com",
                "555-1212",
                "1 Main St",
                "Albany",
                ContactInfo.State.NY,
                "12345"
        );
    }

    @Test
    void buildsEmployeeRecord() {
        var e = new Employee(
                "E-1001",
                contact("Alice"),
                contact("Bob"),
                contact("Carol"),
                Role.IT_LEVEL_1,
                LocalDate.of(2025, 1, 15)
        );

        assertEquals("E-1001", e.employeeID());
        assertEquals(Role.IT_LEVEL_1, e.role());
        assertEquals("Alice", e.employee().firstName());
    }

    @Test
    void builderCanCreateEmployee() {
        var e = new Employee.Builder()
                .employeeID("E-1002")
                .employee(contact("Dana"))
                .emergencyContact1(contact("Evan"))
                .emergencyContact2(contact("Fran"))
                .role(Role.WAREHOUSE_PICKER)
                .hireDate(LocalDate.of(2024, 10, 1))
                .build();

        assertEquals(Role.WAREHOUSE_PICKER, e.role());
    }

    @Test
    void employeeIdMustNotBeBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Employee(
                "   ",
                contact("Alice"),
                contact("Bob"),
                null,
                Role.IT_LEVEL_1,
                LocalDate.now()
        ));
    }
}
