package io.github.bsmichael.rostermanagement.model;

import org.junit.Assert;
import org.junit.Test;

public class StateTest {

    @Test
    public void testDeriveState_AL() {
        Assert.assertEquals(State.ALABAMA, State.deriveState("AL"));
    }

    @Test
    public void testDeriveState_FL() {
        Assert.assertEquals(State.FLORIDA, State.deriveState("FL"));
    }

    @Test
    public void testDeriveState_NC() {
        Assert.assertEquals(State.NORTH_CAROLINA, State.deriveState("NC"));
    }

    @Test
    public void testDeriveState_SC() {
        Assert.assertEquals(State.SOUTH_CAROLINA, State.deriveState("SC"));
    }

    @Test
    public void testDeriveState_TN() {
        Assert.assertEquals(State.TENNESSEE, State.deriveState("TN"));
    }

    @Test
    public void testDeriveState_GA() {
        Assert.assertEquals(State.GEORGIA, State.deriveState("GA"));
    }

    @Test
    public void testGetDisplayString_GA() {
        Assert.assertEquals("GA", State.getDisplayString(State.GEORGIA));
    }

    @Test
    public void testGetDisplayString_AL() {
        Assert.assertEquals("AL", State.getDisplayString(State.ALABAMA));
    }

    @Test
    public void testGetDisplayString_FL() {
        Assert.assertEquals("FL", State.getDisplayString(State.FLORIDA));
    }

    @Test
    public void testGetDisplayString_NC() {
        Assert.assertEquals("NC", State.getDisplayString(State.NORTH_CAROLINA));
    }

    @Test
    public void testGetDisplayString_SC() {
        Assert.assertEquals("SC", State.getDisplayString(State.SOUTH_CAROLINA));
    }

    @Test
    public void testGetDisplayString_TN() {
        Assert.assertEquals("TN", State.getDisplayString(State.TENNESSEE));
    }

    @Test
    public void testFromDisplayString_AL() {
        Assert.assertEquals(State.ALABAMA, State.fromDisplayString("AL"));
    }

    @Test
    public void testFromDisplayString_FL() {
        Assert.assertEquals(State.FLORIDA, State.fromDisplayString("FL"));
    }

    @Test
    public void testFromDisplayString_NC() {
        Assert.assertEquals(State.NORTH_CAROLINA, State.fromDisplayString("NC"));
    }

    @Test
    public void testFromDisplayString_SC() {
        Assert.assertEquals(State.SOUTH_CAROLINA, State.fromDisplayString("SC"));
    }

    @Test
    public void testFromDisplayString_TN() {
        Assert.assertEquals(State.TENNESSEE, State.fromDisplayString("TN"));
    }

    @Test
    public void testFromDisplayString_GA() {
        Assert.assertEquals(State.GEORGIA, State.fromDisplayString("GA"));
    }

}
