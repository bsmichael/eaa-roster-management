package io.github.bsmichael.rostermanagement.model;

import org.junit.Assert;
import org.junit.Test;

public class WebAdminAccessTest {

    @Test
    public void testGetDisplayString_ChapterAdmin() {
        Assert.assertEquals("2", WebAdminAccess.getDisplayString(WebAdminAccess.CHAPTER_ADMIN));
    }

    @Test
    public void testGetDisplayString_ChapterReadOnly() {
        Assert.assertEquals("3", WebAdminAccess.getDisplayString(WebAdminAccess.CHAPTER_READONLY));
    }

    @Test
    public void testGetDisplayString_NoAccess() {
        Assert.assertEquals("4", WebAdminAccess.getDisplayString(WebAdminAccess.NO_ACCESS));
    }

    @Test
    public void testFromDisplayString_ChapterAdmin() {
        Assert.assertEquals(WebAdminAccess.CHAPTER_ADMIN, WebAdminAccess.fromDisplayString("Chapter Admin"));
    }

    @Test
    public void testFromDisplayString_ChapterReadOnly() {
        Assert.assertEquals(WebAdminAccess.CHAPTER_READONLY, WebAdminAccess.fromDisplayString("Chapter Read Only"));
    }

    @Test
    public void testFromDisplayString_NoAccess() {
        Assert.assertEquals(WebAdminAccess.NO_ACCESS, WebAdminAccess.fromDisplayString("No Access"));
    }

}
