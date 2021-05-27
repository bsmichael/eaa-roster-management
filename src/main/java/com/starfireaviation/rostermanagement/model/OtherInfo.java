package com.starfireaviation.rostermanagement.model;

import com.starfireaviation.rostermanagement.util.OtherInfoBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OtherInfo {

    private String raw;

    private String rfid;

    private String slack;

    private String description;

    private Long numOfFamily;

    private List<String> family;

    public OtherInfo(String otherInfo) {
        final OtherInfoBuilder builder = new OtherInfoBuilder();
        builder.setRaw(otherInfo);
        slack = builder.getSlack();
        rfid = builder.getRfid();
        description = builder.getAdditionalInfo();
        numOfFamily = builder.getNumberOfFamily();
        if (builder.getAdditionalFamily() != null) {
            family = Arrays.asList(builder.getAdditionalFamily().split(","));
        }
        raw = builder.getRaw();
    }

    public String getRfid() {
        return rfid;
    }

    public void setRfid(final String rfid) {
        this.rfid = rfid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getSlack() {
        return slack;
    }

    public void setSlack(final String slack) {
        this.slack = slack;
    }

    public List<String> getFamily() {
        return family;
    }

    public void setFamily(final List<String> family) {
        this.family = family;
    }

    public Long getNumOfFamily() {
        return numOfFamily;
    }

    public void setNumOfFamily(Long numOfFamily) {
        this.numOfFamily = numOfFamily;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    public String toString() {
        final List<String> elements = new ArrayList<>();
        elements.add(String.format("RFID=[%s]", rfid));
        elements.add(String.format("Slack=[%s]", slack));
        elements.add(String.format("Family=[%s]", family.stream().collect(Collectors.joining(", "))));
        elements.add(String.format("Description=[%s]", description));
        return elements.stream().collect(Collectors.joining("; "));
    }
}
