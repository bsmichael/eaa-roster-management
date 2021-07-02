package io.github.bsmichael.rostermanagement.model;

public enum Gender {

    /**
     * Male.
     */
    MALE,

    /**
     * Female.
     */
    FEMALE,

    /**
     * Unknown.
     */
    UNKNOWN;

    public static String getDisplayString(Gender gender) {
        if (MALE.equals(gender)) {
            return "Male";
        } else if (FEMALE.equals(gender)) {
            return "Female";
        } else {
            return "Unknown";
        }
    }

    public static Gender fromDisplayString(final String displayString) {
        if ("Male".equalsIgnoreCase(displayString)) {
            return MALE;
        } else if ("Female".equalsIgnoreCase(displayString)) {
            return FEMALE;
        }
        return UNKNOWN;
    }
}
