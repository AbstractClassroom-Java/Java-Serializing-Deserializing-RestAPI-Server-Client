package io.github.nathanjrussell;

import io.github.nathanjrussell.humanresources.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void allRolesRespectPayConstraints() {
        for (Role role : Role.values()) {
            assertTrue(role.minimumPay() >= 50_000, role.name() + " minimumPay too low");
            assertTrue(role.maximumPay() <= 170_000, role.name() + " maximumPay too high");
            assertTrue(role.minimumPay() <= role.maximumPay(), role.name() + " pay band invalid");
            assertFalse(role.title().isBlank());
            assertFalse(role.description().isBlank());
        }
    }
}
