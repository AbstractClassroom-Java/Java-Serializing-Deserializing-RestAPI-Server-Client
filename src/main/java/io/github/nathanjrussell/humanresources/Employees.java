package io.github.nathanjrussell.humanresources;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// In-memory employee collection keyed by employeeID.
public final class Employees {

    private final Map<String, Employee> employeesById;

    public Employees() {
        this.employeesById = new HashMap<>();
    }

    private Employees(Map<String, Employee> employeesById) {
        this.employeesById = new HashMap<>(employeesById);
    }

    // Add/replace employee by employeeID.
    public void add(Employee employee) {
        Objects.requireNonNull(employee, "employee");
        employeesById.put(employee.employeeID(), employee);
    }

    // Look up by employeeID (null if missing).
    public Employee lookUp(String employeeId) {
        if (employeeId == null) {
            return null;
        }
        return employeesById.get(employeeId);
    }

    // Return all IDs.
    public String[] getIDs() {
        return employeesById.keySet().toArray(String[]::new);
    }

    public Collection<Employee> values() {
        return employeesById.values();
    }

    // Serialize as {"employees": {"E-1": {...}, ...}}
    public String toJson() {
        var root = JsonUtil.MAPPER.createObjectNode();
        var employeesNode = JsonUtil.MAPPER.createObjectNode();

        for (var entry : employeesById.entrySet()) {
            employeesNode.set(entry.getKey(), parseNode(entry.getValue().toJson()));
        }

        root.set("employees", employeesNode);
        return JsonUtil.writeValueAsString(root);
    }

    public static Employees fromJson(String json) {
        JsonNode root = JsonUtil.readValue(json, JsonNode.class);
        if (root == null || !root.isObject()) {
            throw new IllegalArgumentException("Employees JSON must be an object");
        }

        JsonNode employeesNode = root.get("employees");
        if (employeesNode == null || employeesNode.isNull()) {
            throw new IllegalArgumentException("employees must not be null");
        }
        if (!employeesNode.isObject()) {
            throw new IllegalArgumentException("employees must be a JSON object keyed by employeeId");
        }

        Map<String, Employee> map = new HashMap<>();
        employeesNode.fields().forEachRemaining(e -> {
            String employeeId = e.getKey();
            JsonNode employeeJsonNode = e.getValue();
            Employee employee = Employee.fromJson(employeeJsonNode.toString());

            // Key must match the payload.
            if (!Objects.equals(employeeId, employee.employeeID())) {
                throw new IllegalArgumentException("Employee key '" + employeeId + "' does not match employee.employeeID '" + employee.employeeID() + "'");
            }
            map.put(employeeId, employee);
        });

        return new Employees(map);
    }

    private static JsonNode parseNode(String json) {
        return JsonUtil.readValue(json, JsonNode.class);
    }
}
