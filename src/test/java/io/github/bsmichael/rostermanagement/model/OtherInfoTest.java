package io.github.bsmichael.rostermanagement.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class OtherInfoTest {

    @Test
    public void testSetRfid() {
        String rfid = "1234567890";
        OtherInfo otherInfo = new OtherInfo(null);
        otherInfo.setRfid(rfid);
        Assert.assertEquals(rfid, otherInfo.getRfid());
    }

    @Test
    public void testSetDescription() {
        String description = "some text";
        OtherInfo otherInfo = new OtherInfo(null);
        otherInfo.setDescription(description);
        Assert.assertEquals(description, otherInfo.getDescription());
    }

    @Test
    public void testSetSlack() {
        String slack = "username";
        OtherInfo otherInfo = new OtherInfo(null);
        otherInfo.setSlack(slack);
        Assert.assertEquals(slack, otherInfo.getSlack());
    }

    @Test
    public void testSetFamily() {
        String family = "john, jane";
        OtherInfo otherInfo = new OtherInfo(null);
        otherInfo.setFamily(Arrays.asList(family.split(",")));
        Assert.assertEquals(Arrays.asList(family.split(",")), otherInfo.getFamily());
    }

    @Test
    public void testSetNumOfFamily() {
        OtherInfo otherInfo = new OtherInfo(null);
        otherInfo.setNumOfFamily(1L);
        Assert.assertEquals(java.util.Optional.of(1L).get(), otherInfo.getNumOfFamily());
    }

    @Test
    public void testSetRaw() {
        OtherInfo otherInfo = new OtherInfo(null);
        otherInfo.setRaw("");
        Assert.assertEquals("", otherInfo.getRaw());
    }

    @Test
    public void testToString_Empty() {
        OtherInfo otherInfo = new OtherInfo(null);
        Assert.assertEquals("RFID=[]; Slack=[]; Family=[]; Description=[]", otherInfo.toString());
    }

    @Test
    public void testToString_NotEmpty() {
        OtherInfo otherInfo = new OtherInfo(null);
        otherInfo.setFamily(Arrays.asList("John"));
        otherInfo.setRfid("1234567890");
        otherInfo.setSlack("john");
        otherInfo.setDescription("awesome guy");
        Assert.assertEquals("RFID=[1234567890]; Slack=[john]; Family=[John]; Description=[awesome guy]",
                otherInfo.toString());
    }

}
