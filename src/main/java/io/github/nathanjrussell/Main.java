package io.github.nathanjrussell;

import io.github.nathanjrussell.humanresources.ContactInfo;
import io.github.nathanjrussell.humanresources.Employee;
import io.github.nathanjrussell.humanresources.Employees;
import io.github.nathanjrussell.humanresources.NathanAPI;
import io.github.nathanjrussell.humanresources.Role;

import java.net.URI;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) throws Exception {
        // Expect the rest server to be running in a separate window:
        //   io.github.nathanjrussell.restserver.RestServerMain
        String baseUrl = args.length > 0 && !args[0].isBlank() ? args[0] : "http://52.23.172.152:8080";

        URI writeEndpoint = URI.create(baseUrl + "/employee");
        URI readEndpoint = URI.create(baseUrl + "/employees");

        var api = new NathanAPI();

        Employee e1 = sampleEmployee("E-1001", "Ada", "Lovelace", Role.SOFTWARE_ENGINEER);
        Employee e2 = sampleEmployee("E-1002", "Grace", "Hopper", Role.IT_LEVEL_2);
        Employee e3 = sampleEmployee("E-1003", "Katherine", "Johnson", Role.PROJECT_MANAGER);

        // Write individually (JSON goes over the wire; both client/server use the shared model locally)
//        api.write(writeEndpoint, e1);
//        api.write(writeEndpoint, e2);
        api.write(writeEndpoint, e3);

        // Read back as list
        Employees employees = api.read(readEndpoint);

        System.out.println("Round-trip complete. IDs returned by server:");
        for (String id : employees.getIDs()) {
            Employee emp = employees.lookUp(id);
            System.out.println("- " + id + " => " + emp.employee().firstName() + " " + emp.employee().lastName() + " (" + emp.role().title() + ")");
        }

        // Demonstrate lookUp
//        System.out.println("Look up E-1002 => " + employees.lookUp("E-1002").employee().firstName());

        // Demonstrate that we can take the list and add to another local Employees instance.
        Employees localCopy = new Employees();
        for (String id : employees.getIDs()) {
            localCopy.add(employees.lookUp(id));
        }
        System.out.println("Local copy size (via add): " + localCopy.getIDs().length);
    }

    private static Employee sampleEmployee(String id, String firstName, String lastName, Role role) {
        ContactInfo emp = new ContactInfo(
                firstName,
                lastName,
                firstName.toLowerCase() + "." + lastName.toLowerCase() + "@example.com",
                "555-1212",
                "123 Example St",
                "Louisville",
                ContactInfo.State.KY,
                "40202"
        );

        ContactInfo emergency = new ContactInfo(
                "Emergency",
                "Contact",
                "emergency@example.com",
                "555-9999",
                "123 Example St",
                "Louisville",
                ContactInfo.State.KY,
                "40202-1234"
        );

        return new Employee(
                id,
                emp,
                emergency,
                null,
                role,
                LocalDate.now()
        );
    }
}