package io.github.bsmichael.rostermanagement.model;

import org.junit.Assert;
import org.junit.Test;

public class StatusTest {

    @Test
    public void testActiveStatus() {
        Assert.assertEquals("Active", Status.getDisplayString(Status.ACTIVE));
    }

    @Test
    public void testInactiveStatus() {
        Assert.assertEquals("Inactive", Status.getDisplayString(Status.INACTIVE));
    }
}
