package io.github.bsmichael.rostermanagement.model;

import org.junit.Assert;
import org.junit.Test;

public class PersonTest {

    @Test
    public void testGetNumOfFamily_None() {
        Person person = new Person();
        Assert.assertEquals(java.util.Optional.of(0L).get(), person.getNumOfFamily());
    }

    @Test
    public void testGetNumOfFamily_AtLeastOne() {
        Person person = new Person();
        person.setNumOfFamily(1L);
        Assert.assertEquals(java.util.Optional.of(1L).get(), person.getNumOfFamily());
    }

    @Test
    public void testCompareTo_Same() {
        Person one = new Person();
        Assert.assertEquals(0, one.compareTo(one));
    }

    @Test
    public void testCompareTo_Different() {
        Person one = new Person();
        Person two = new Person();
        two.setFirstName("John");
        Assert.assertEquals(1, one.compareTo(two));
    }
}
