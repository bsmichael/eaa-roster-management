package io.github.bsmichael.rostermanagement.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OtherInfoBuilder {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(OtherInfoBuilder.class);

    /**
     * Family Pattern.
     */
    public static Pattern additionalFamilyPattern = Pattern.compile("Family=\\[(.*?)\\]");

    /**
     * # of Family Pattern.
     */
    public static Pattern numOfFamilyPattern = Pattern.compile("# of Family=\\[(.*?)\\]");

    /**
     * Slack Pattern.
     */
    public static Pattern slackPattern = Pattern.compile("Slack=\\[(.*?)\\]");

    /**
     * RFID Pattern.
     */
    public static Pattern rfidPattern = Pattern.compile("RFID=\\[(.*?)\\]");

    /**
     * Additional Info Pattern.
     */
    public static Pattern additionalInfoPattern = Pattern.compile("Additional Info=\\[(.*?)\\]");

    /**
     * Additional Family.
     */
    private String additionalFamily;

    /**
     * Slack.
     */
    private String slack;

    /**
     * Number of Family.
     */
    private Long numOfFamily;

    /**
     * RFID.
     */
    private String rfid;

    /**
     * Additional Information.
     */
    private String additionalInfo;

    /**
     * Default constructor.
     */
    public OtherInfoBuilder() {
        // Default constructor
    }

    public String getAdditionalFamily() {
        return additionalFamily;
    }

    public void setAdditionalFamily(String additionalFamily) {
        this.additionalFamily = additionalFamily;
    }

    public Long getNumberOfFamily() {
        return numOfFamily;
    }

    public void setNumberOfFamily(Long numOfFamily) {
        this.numOfFamily = numOfFamily;
    }

    public String getSlack() {
        return slack;
    }

    public void setSlack(String slack) {
        this.slack = slack;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getRfid() {
        return rfid;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
    }

    public String getRaw() {
        final List<String> parts = new ArrayList<>();
        if (additionalFamily != null) {
            parts.add(String.format("Family=[%s]", additionalFamily));
        }
        if (numOfFamily != null) {
            parts.add(String.format("# of Family=[%s]", numOfFamily));
        }
        if (slack != null) {
            parts.add(String.format("Slack=[%s]", slack));
        }
        if (rfid != null) {
            parts.add(String.format("RFID=[%s]", rfid));
        }
        if (additionalInfo != null) {
            parts.add(String.format("Additional Info=[%s]", additionalInfo));
        }
        return String.join("; ", parts);
    }

    public void setRaw(String raw) {
        boolean matched = false;
        if (raw != null) {
            final Matcher additionalFamilyMatcher = additionalFamilyPattern.matcher(raw);
            if (additionalFamilyMatcher.find()) {
                matched = true;
                setAdditionalFamily(additionalFamilyMatcher.group(1));
                LOGGER.debug("Set additional family to ["+getAdditionalFamily()+"]");
            }
            final Matcher numOfFamilyMatcher = numOfFamilyPattern.matcher(raw);
            if (numOfFamilyMatcher.find()) {
                try {
                    setNumberOfFamily(Long.parseLong(numOfFamilyMatcher.group(1)));
                    matched = true;
                    LOGGER.debug("Set number of family to ["+getNumberOfFamily()+"]");
                } catch (NumberFormatException nfe) {
                    LOGGER.debug("Unable to parse number of family value=["+numOfFamilyMatcher.group(1)+"]");
                }
            }
            final Matcher slackMatcher = slackPattern.matcher(raw);
            if (slackMatcher.find()) {
                matched = true;
                setSlack(slackMatcher.group(1));
                LOGGER.debug("Set Slack to ["+getSlack()+"]");
            }
            final Matcher rfidMatcher = rfidPattern.matcher(raw);
            if (rfidMatcher.find()) {
                matched = true;
                setRfid(rfidMatcher.group(1));
                LOGGER.debug("Set RFID to ["+getRfid()+"]");
            }
            final Matcher additionalInfoMatcher = additionalInfoPattern.matcher(raw);
            if (additionalInfoMatcher.find()) {
                matched = true;
                setAdditionalInfo(additionalInfoMatcher.group(1));
                LOGGER.debug("Set additional info to ["+getAdditionalInfo()+"]");
            }
            if (!matched) {
                LOGGER.debug("No patterns matched.  Setting additional info to ["+raw+"]");
                setAdditionalInfo(raw);
            }
        }
    }

    @Override
    public String toString() {
        return getRaw();
    }
}
