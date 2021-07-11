package io.github.bsmichael.rostermanagement.model;

import org.junit.Assert;
import org.junit.Test;

public class GenderTest {

    @Test
    public void testGetDisplayString_Male() {
        Assert.assertEquals("Male", Gender.getDisplayString(Gender.MALE));
    }

    @Test
    public void testGetDisplayString_Female() {
        Assert.assertEquals("Female", Gender.getDisplayString(Gender.FEMALE));
    }

    @Test
    public void testGetDisplayString_Unknown() {
        Assert.assertEquals("Unknown", Gender.getDisplayString(Gender.UNKNOWN));
    }

    @Test
    public void testFromDisplayString_Male() {
        Assert.assertEquals(Gender.MALE, Gender.fromDisplayString("Male"));
    }

    @Test
    public void testFromDisplayString_Female() {
        Assert.assertEquals(Gender.FEMALE, Gender.fromDisplayString("Female"));
    }

    @Test
    public void testFromDisplayString_Unknown() {
        Assert.assertEquals(Gender.UNKNOWN, Gender.fromDisplayString("Unknown"));
    }
}
