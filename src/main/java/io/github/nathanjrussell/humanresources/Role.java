package io.github.nathanjrussell.humanresources;

import com.fasterxml.jackson.databind.JsonNode;

// Role with salary band and description.
public enum Role {
    IT_LEVEL_1("IT Level 1", 60_000, 85_000, "Entry-level IT support / junior systems support"),
    IT_LEVEL_2("IT Level 2", 80_000, 115_000, "Intermediate IT specialist / systems administrator"),

    FORKLIFT_OPERATOR("Forklift Operator", 55_000, 75_000, "Operates forklifts and other powered industrial trucks"),
    WAREHOUSE_PICKER("Warehouse Picker", 50_000, 68_000, "Picks and packs items, prepares shipments"),

    HR_LEVEL_1("HR Level 1", 58_000, 82_000, "HR coordinator / recruiting support"),
    HR_LEVEL_2("HR Level 2", 78_000, 110_000, "HR generalist / benefits and employee relations"),

    OPERATIONS_SUPERVISOR("Operations Supervisor", 70_000, 105_000, "Supervises day-to-day warehouse operations"),
    FINANCE_ANALYST("Finance Analyst", 75_000, 120_000, "Budgeting, forecasting, and financial reporting"),
    SALES_ASSOCIATE("Sales Associate", 50_000, 95_000, "Customer relationships and account management"),
    SOFTWARE_ENGINEER("Software Engineer", 95_000, 165_000, "Builds and maintains internal software systems"),
    SECURITY_OFFICER("Security Officer", 50_000, 72_000, "Site security, access control, incident reporting"),
    MAINTENANCE_TECHNICIAN("Maintenance Technician", 60_000, 90_000, "Facility maintenance and equipment repair"),
    PROJECT_MANAGER("Project Manager", 90_000, 150_000, "Leads cross-functional projects and delivery"),
    DIRECTOR("Director", 130_000, 170_000, "Leads a department, strategy, staffing, and budgeting");

    private final String title;
    private final int minimumPay;
    private final int maximumPay;
    private final String description;

    Role(String title, int minimumPay, int maximumPay, String description) {
        this.title = requireNonBlank(title, "title");
        this.minimumPay = minimumPay;
        this.maximumPay = maximumPay;
        this.description = requireNonBlank(description, "description");

        validatePayBand(this.minimumPay, this.maximumPay);
    }

    public String title() {
        return title;
    }

    public int minimumPay() {
        return minimumPay;
    }

    public int maximumPay() {
        return maximumPay;
    }

    public String description() {
        return description;
    }

    public String toJson() {
        // Serialize full details.
        var node = JsonUtil.MAPPER.createObjectNode();
        node.put("name", name());
        node.put("title", title);
        node.put("minimumPay", minimumPay);
        node.put("maximumPay", maximumPay);
        node.put("description", description);
        return JsonUtil.writeValueAsString(node);
    }

    public static Role fromJson(String json) {
        JsonNode node = JsonUtil.readValue(json, JsonNode.class);
        if (node == null || node.isNull()) {
            throw new IllegalArgumentException("Role JSON must not be null");
        }

        if (node.isTextual()) {
            return Role.valueOf(node.asText());
        }

        if (node.isObject()) {
            JsonNode nameNode = node.get("name");
            if (nameNode != null && nameNode.isTextual()) {
                return Role.valueOf(nameNode.asText());
            }
        }

        throw new IllegalArgumentException("Could not parse Role from JSON");
    }

    private static void validatePayBand(int min, int max) {
        if (min < 50_000) {
            throw new IllegalArgumentException("minimumPay must be at least 50000");
        }
        if (max > 170_000) {
            throw new IllegalArgumentException("maximumPay must be at most 170000");
        }
        if (min > max) {
            throw new IllegalArgumentException("minimumPay must be <= maximumPay");
        }
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
}
