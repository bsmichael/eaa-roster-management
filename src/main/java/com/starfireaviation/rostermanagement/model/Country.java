package com.starfireaviation.rostermanagement.model;

public enum Country {

    /**
     * United States of America.
     */
    USA;

    /**
     * Converts to displayable string.
     *
     * @param country Country
     * @return displayable string
     */
    public static String toDisplayString(final Country country) {
        return "USA";
    }

    /**
     * Converts from displayable string.
     *
     * @param displayString displayable string
     * @return Country
     */
    public static Country fromDisplayString(final String displayString) {
        return USA;
    }
}
