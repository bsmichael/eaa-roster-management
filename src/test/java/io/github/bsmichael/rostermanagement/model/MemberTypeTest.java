package io.github.bsmichael.rostermanagement.model;

import org.junit.Assert;
import org.junit.Test;

public class MemberTypeTest {

    private MemberType regularMemberType = MemberType.Regular;

    private MemberType familyMemberType = MemberType.Family;

    @Test
    public void testGetValue_Regular() {
        Assert.assertEquals("Regular", regularMemberType.getValue());
    }

    @Test
    public void testGetValue_Family() {
        Assert.assertEquals("Family", familyMemberType.getValue());
    }

    @Test
    public void testFromString_Regular() {
        Assert.assertEquals(MemberType.Regular, MemberType.fromString("Regular"));
    }

    @Test
    public void testFromString_Family() {
        Assert.assertEquals(MemberType.Family, MemberType.fromString("Family"));
    }

    @Test
    public void testFromString_Lifetime() {
        Assert.assertEquals(MemberType.Lifetime, MemberType.fromString("Lifetime"));
    }

    @Test
    public void testFromString_Honorary() {
        Assert.assertEquals(MemberType.Honorary, MemberType.fromString("Honorary"));
    }

    @Test
    public void testFromString_Student() {
        Assert.assertEquals(MemberType.Student, MemberType.fromString("Student"));
    }

    @Test
    public void testFromString_Prospect() {
        Assert.assertEquals(MemberType.Prospect, MemberType.fromString("Prospect"));
    }

    @Test
    public void testFromString_Nonmember() {
        Assert.assertEquals(MemberType.NonMember, MemberType.fromString("Non-Member"));
    }

    @Test
    public void testFromString_Unknown() {
        Assert.assertNull(MemberType.fromString("Unknown"));
    }

    @Test
    public void testToDisplayString_Regular() {
        Assert.assertEquals("Regular", MemberType.toDisplayString(MemberType.Regular));
    }

    @Test
    public void testToDisplayString_Family() {
        Assert.assertEquals("Family", MemberType.toDisplayString(MemberType.Family));
    }

    @Test
    public void testToDisplayString_Lifetime() {
        Assert.assertEquals("Lifetime", MemberType.toDisplayString(MemberType.Lifetime));
    }

    @Test
    public void testToDisplayString_Honorary() {
        Assert.assertEquals("Honorary", MemberType.toDisplayString(MemberType.Honorary));
    }

    @Test
    public void testToDisplayString_Student() {
        Assert.assertEquals("Student", MemberType.toDisplayString(MemberType.Student));
    }

    @Test
    public void testToDisplayString_Prospect() {
        Assert.assertEquals("Prospect", MemberType.toDisplayString(MemberType.Prospect));
    }

    @Test
    public void testToDisplayString_NonMember() {
        Assert.assertEquals("Non-Member", MemberType.toDisplayString(MemberType.NonMember));
    }

}
