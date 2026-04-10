package io.github.nathanjrussell.humanresources;

import java.util.Objects;
import java.util.regex.Pattern;

// Contact information record.
public record ContactInfo(
        String firstName,
        String lastName,
        String email,
        String telephone,
        String address,
        String city,
        State state,
        String zipcode
) {
    private static final Pattern ZIPCODE_PATTERN = Pattern.compile("^\\d{5}(-\\d{4})?$");

    public ContactInfo {
        firstName = requireNonBlank(firstName, "firstName");
        lastName = requireNonBlank(lastName, "lastName");
        email = requireNonBlank(email, "email");
        telephone = requireNonBlank(telephone, "telephone");
        address = requireNonBlank(address, "address");
        city = requireNonBlank(city, "city");
        state = Objects.requireNonNull(state, "state");
        zipcode = validateZipcode(zipcode);
    }

    // US states + DC.
    public enum State {
        AL, AK, AZ, AR, CA, CO, CT, DE, FL, GA,
        HI, ID, IL, IN, IA, KS, KY, LA, ME, MD,
        MA, MI, MN, MS, MO, MT, NE, NV, NH, NJ,
        NM, NY, NC, ND, OH, OK, OR, PA, RI, SC,
        SD, TN, TX, UT, VT, VA, WA, WV, WI, WY,
        DC
    }

    public static final class Builder {
        private String firstName;
        private String lastName;
        private String email;
        private String telephone;
        private String address;
        private String city;
        private State state;
        private String zipcode;

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder telephone(String telephone) {
            this.telephone = telephone;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder state(State state) {
            this.state = state;
            return this;
        }

        public Builder zipcode(String zipcode) {
            this.zipcode = zipcode;
            return this;
        }

        public ContactInfo build() {
            return new ContactInfo(firstName, lastName, email, telephone, address, city, state, zipcode);
        }
    }

    public String toJson() {
        return JsonUtil.writeValueAsString(this);
    }

    public static ContactInfo fromJson(String json) {
        return JsonUtil.readValue(json, ContactInfo.class);
    }

    // Zipcode must be 12345 or 12345-6789.
    public static String validateZipcode(String zipcode) {
        zipcode = requireNonBlank(zipcode, "zipcode");
        if (!ZIPCODE_PATTERN.matcher(zipcode).matches()) {
            throw new IllegalArgumentException("zipcode must be 5 digits or ZIP+4 (e.g., 12345 or 12345-6789)");
        }
        return zipcode;
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
