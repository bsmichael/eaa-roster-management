package com.starfireaviation.rostermanagement.model;

public enum Status {
    /**
     * Active.
     */
    ACTIVE,
    /**
     * Inactive.
     */
    INACTIVE;

    public static String getDisplayString(Status status) {
        if (ACTIVE.equals(status)) {
            return "Active";
        } else {
            return "Inactive";
        }
    }
}
