package io.github.nathanjrussell;

import io.github.nathanjrussell.humanresources.ContactInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContactInfoTest {

    @Test
    void validZipcode_acceptsFiveDigits() {
        var c = new ContactInfo(
                "Ada",
                "Lovelace",
                "ada@example.com",
                "555-1212",
                "1 Main St",
                "Somewhere",
                ContactInfo.State.NY,
                "12345"
        );

        assertEquals("12345", c.zipcode());
    }

    @Test
    void validZipcode_acceptsZipPlus4() {
        var c = new ContactInfo(
                "Ada",
                "Lovelace",
                "ada@example.com",
                "555-1212",
                "1 Main St",
                "Somewhere",
                ContactInfo.State.NY,
                "12345-6789"
        );

        assertEquals("12345-6789", c.zipcode());
    }

    @Test
    void invalidZipcode_rejectsBadFormats() {
        assertThrows(IllegalArgumentException.class, () -> new ContactInfo(
                "Ada",
                "Lovelace",
                "ada@example.com",
                "555-1212",
                "1 Main St",
                "Somewhere",
                ContactInfo.State.NY,
                "1234"
        ));

        assertThrows(IllegalArgumentException.class, () -> new ContactInfo(
                "Ada",
                "Lovelace",
                "ada@example.com",
                "555-1212",
                "1 Main St",
                "Somewhere",
                ContactInfo.State.NY,
                "12345-678"
        ));

        assertThrows(IllegalArgumentException.class, () -> new ContactInfo(
                "Ada",
                "Lovelace",
                "ada@example.com",
                "555-1212",
                "1 Main St",
                "Somewhere",
                ContactInfo.State.NY,
                "12345 6789"
        ));
    }

    @Test
    void builder_buildsContactInfo() {
        var c = new ContactInfo.Builder()
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada@example.com")
                .telephone("555-1212")
                .address("1 Main St")
                .city("Somewhere")
                .state(ContactInfo.State.NY)
                .zipcode("12345")
                .build();

        assertEquals("Ada", c.firstName());
        assertEquals(ContactInfo.State.NY, c.state());
    }

    @Test
    void builderStillValidatesZipcode() {
        assertThrows(IllegalArgumentException.class, () -> new ContactInfo.Builder()
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada@example.com")
                .telephone("555-1212")
                .address("1 Main St")
                .city("Somewhere")
                .state(ContactInfo.State.NY)
                .zipcode("ABCDE")
                .build());
    }
}
