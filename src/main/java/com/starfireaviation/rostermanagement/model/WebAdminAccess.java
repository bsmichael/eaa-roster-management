package com.starfireaviation.rostermanagement.model;

public enum WebAdminAccess {
    /**
     * Chapter Admin.
     */
    CHAPTER_ADMIN,
    /**
     * Chapter Read Only.
     */
    CHAPTER_READONLY,
    /**
     * No Access.
     */
    NO_ACCESS;

    public static String getDisplayString(WebAdminAccess admin) {
        if (CHAPTER_ADMIN.equals(admin)) {
            return "2";
        } else if (CHAPTER_READONLY.equals(admin)) {
            return "1";
        }
        return "0";
    }

    public static WebAdminAccess fromDisplayString(final String displayString) {
        if ("Chapter Admin".equalsIgnoreCase(displayString)) {
            return CHAPTER_ADMIN;
        } else if ("Chapter Read Only".equalsIgnoreCase(displayString)) {
            return CHAPTER_READONLY;
        }
        return NO_ACCESS;
    }
}
