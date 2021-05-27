package com.starfireaviation.rostermanagement.model;

public enum MemberType {

    /**
     * Regular.
     */
    Regular("Regular"),
    /**
     * Family.
     */
    Family("Family"),
    /**
     * Lifetime.
     */
    Lifetime("Lifetime"),
    /**
     * Honorary.
     */
    Honorary("Honorary"),
    /**
     * Student.
     */
    Student("Student"),
    /**
     * Prospect.
     */
    Prospect("Prospect"),
    /**
     * Non-member.
     */
    NonMember("Non-Member");

    private final String value;

    private MemberType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MemberType fromString(String text) {
        for (MemberType b : MemberType.values()) {
            if (b.value.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

    public static String toDisplayString(final MemberType memberType) {
        if (Regular.equals(memberType)) {
            return "Regular";
        } else if (Family.equals(memberType)) {
            return "Family";
        } else if (Lifetime.equals(memberType)) {
            return "Lifetime";
        } else if (Honorary.equals(memberType)) {
            return "Honorary";
        } else if (Student.equals(memberType)) {
            return "Student";
        } else if (Prospect.equals(memberType)) {
            return "Prospect";
        }
        return "Non-Member";
    }
}

