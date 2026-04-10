package io.github.nathanjrussell.humanresources;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.util.Objects;

// Employee record.
public record Employee(
        String employeeID,
        ContactInfo employee,
        ContactInfo emergencyContact1,
        ContactInfo emergencyContact2,
        Role role,
        LocalDate hireDate
) {
    public Employee {
        employeeID = requireNonBlank(employeeID, "employeeID");
        employee = Objects.requireNonNull(employee, "employee");
        emergencyContact1 = Objects.requireNonNull(emergencyContact1, "emergencyContact1");
        // emergencyContact2 may be null
        role = Objects.requireNonNull(role, "role");
        hireDate = Objects.requireNonNull(hireDate, "hireDate");
    }

    public String toJson() {
        // Build JSON by chaining ContactInfo/Role serializers.
        var node = JsonUtil.MAPPER.createObjectNode();
        node.put("employeeID", employeeID);
        node.set("employee", parseNode(employee.toJson()));
        node.set("emergencyContact1", parseNode(emergencyContact1.toJson()));
        if (emergencyContact2 != null) {
            node.set("emergencyContact2", parseNode(emergencyContact2.toJson()));
        } else {
            node.putNull("emergencyContact2");
        }
        node.set("role", parseNode(role.toJson()));
        node.put("hireDate", hireDate.toString());
        return JsonUtil.writeValueAsString(node);
    }

    public static Employee fromJson(String json) {
        var node = JsonUtil.readValue(json, JsonNode.class);
        if (node == null || !node.isObject()) {
            throw new IllegalArgumentException("Employee JSON must be an object");
        }

        String employeeID = requiredText(node, "employeeID");

        ContactInfo employee = ContactInfo.fromJson(requiredNode(node, "employee").toString());
        ContactInfo emergency1 = ContactInfo.fromJson(requiredNode(node, "emergencyContact1").toString());

        ContactInfo emergency2 = null;
        JsonNode emergency2Node = node.get("emergencyContact2");
        if (emergency2Node != null && !emergency2Node.isNull()) {
            emergency2 = ContactInfo.fromJson(emergency2Node.toString());
        }

        Role role = Role.fromJson(requiredNode(node, "role").toString());
        LocalDate hireDate = LocalDate.parse(requiredText(node, "hireDate"));

        return new Employee(employeeID, employee, emergency1, emergency2, role, hireDate);
    }

    private static JsonNode parseNode(String json) {
        return JsonUtil.readValue(json, JsonNode.class);
    }

    private static JsonNode requiredNode(JsonNode parent, String fieldName) {
        JsonNode child = parent.get(fieldName);
        if (child == null || child.isNull()) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        return child;
    }

    private static String requiredText(JsonNode parent, String fieldName) {
        JsonNode child = requiredNode(parent, fieldName);
        if (!child.isTextual()) {
            throw new IllegalArgumentException(fieldName + " must be a string");
        }
        return child.asText();
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public static final class Builder {
        private String employeeID;
        private ContactInfo employee;
        private ContactInfo emergencyContact1;
        private ContactInfo emergencyContact2;
        private Role role;
        private LocalDate hireDate;

        public Builder employeeID(String employeeID) {
            this.employeeID = employeeID;
            return this;
        }

        public Builder employee(ContactInfo employee) {
            this.employee = employee;
            return this;
        }

        public Builder emergencyContact1(ContactInfo emergencyContact1) {
            this.emergencyContact1 = emergencyContact1;
            return this;
        }

        public Builder emergencyContact2(ContactInfo emergencyContact2) {
            this.emergencyContact2 = emergencyContact2;
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Builder hireDate(LocalDate hireDate) {
            this.hireDate = hireDate;
            return this;
        }

        public Employee build() {
            return new Employee(employeeID, employee, emergencyContact1, emergencyContact2, role, hireDate);
        }
    }
}
