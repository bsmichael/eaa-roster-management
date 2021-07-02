package io.github.bsmichael.rostermanagement.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@ToString
public class Person implements Comparable<Person> {

    /**
     * Roster management system ID.
     */
    @Getter
    @Setter
    private Long rosterId;

    /**
     * Assigned RFID.
     */
    @Getter
    @Setter
    private String rfid;

    /**
     * Slack handle.
     */
    @Getter
    @Setter
    private String slack;

    /**
     * First Name.
     */
    @Getter
    @Setter
    private String firstName;

    /**
     * Last Name.
     */
    @Getter
    @Setter
    private String lastName;

    /**
     * Nickname.
     */
    @Getter
    @Setter
    private String nickname;

    /**
     * Username.
     */
    @Getter
    @Setter
    private String username;

    /**
     * Spouse.
     */
    @Getter
    @Setter
    private String spouse;

    /**
     * Gender.
     */
    @Getter
    @Setter
    private Gender gender;

    /**
     * Member Type.
     */
    @Getter
    @Setter
    private MemberType memberType;

    /**
     * Status.
     */
    @Getter
    @Setter
    private Status status;

    /**
     * Web Admin Access.
     */
    @Getter
    @Setter
    private WebAdminAccess webAdminAccess;

    /**
     * Address Line 1.
     */
    @Getter
    @Setter
    private String addressLine1;

    /**
     * Address Line 2.
     */
    @Getter
    @Setter
    private String addressLine2;

    /**
     * City.
     */
    @Getter
    @Setter
    private String city;

    /**
     * State.
     */
    @Getter
    @Setter
    private State state;

    /**
     * Zip Code.
     */
    @Getter
    @Setter
    private String zipCode;

    /**
     * Country.
     */
    @Getter
    @Setter
    private Country country;

    /**
     * Birth Date.
     */
    @Getter
    @Setter
    private String birthDate;

    /**
     * Joined Date.
     */
    @Getter
    @Setter
    private String joined;

    /**
     * Other Information.
     *
     * "RFID=[ABC123ZXY43221]; Slack=[@brian]; Family=[Jennifer Michael, Billy Michael]; # of Family=[2]; Additional Info=[some random text]"
     */
    @Getter
    @Setter
    private String otherInfo;

    /**
     * Family.
     */
    @Getter
    @Setter
    private String family;

    /**
     * Num of Family.
     */
    @Setter
    private Long numOfFamily;

    /**
     * AdditionalInfo.
     */
    @Getter
    @Setter
    private String additionalInfo;

    /**
     * Home Phone.
     */
    @Getter
    @Setter
    private String homePhone;

    /**
     * Ratings.
     */
    @Getter
    @Setter
    private String ratings;

    /**
     * Aircraft Owned.
     */
    @Getter
    @Setter
    private String aircraftOwned;

    /**
     * Aircraft Project.
     */
    @Getter
    @Setter
    private String aircraftProject;

    /**
     * Aircraft Built.
     */
    @Getter
    @Setter
    private String aircraftBuilt;

    /**
     * IMC Club.
     */
    @Getter
    @Setter
    private boolean imcClub = Boolean.FALSE;

    /**
     * VMC Club.
     */
    @Getter
    @Setter
    private boolean vmcClub = Boolean.FALSE;

    /**
     * YE Pilot.
     */
    @Getter
    @Setter
    private boolean yePilot = Boolean.FALSE;

    /**
     * YE Volunteer.
     */
    @Getter
    @Setter
    private boolean yeVolunteer = Boolean.FALSE;

    /**
     * Eagle Pilot.
     */
    @Getter
    @Setter
    private boolean eaglePilot = Boolean.FALSE;

    /**
     * Eagle Volunteer.
     */
    @Getter
    @Setter
    private boolean eagleVolunteer = Boolean.FALSE;

    /**
     * EAA Membership Expiration Date.
     */
    @Getter
    @Setter
    private String eaaExpiration;

    /**
     * Youth Protection Expiration Date.
     */
    @Getter
    @Setter
    private String youthProtection;

    /**
     * Background Check Expiration Date.
     */
    @Getter
    @Setter
    private String backgroundCheck;

    /**
     * EAA Number.
     */
    @Getter
    @Setter
    private String eaaNumber;

    /**
     * Email.
     */
    @Getter
    @Setter
    private String email;

    /**
     * Cell Phone.
     */
    @Getter
    @Setter
    private String cellPhone;

    /**
     * Membership Expiration.
     */
    @Getter
    @Setter
    private Date expiration;

    public Long getNumOfFamily() {
        if (numOfFamily == null) {
            return 0L;
        }
        return numOfFamily;
    }

    /**
     * {@inheritDoc} Required implementation.
     */
    @Override
    public int compareTo(final Person other) {
        if (equals(other)) {
            return 0;
        }
        return 1;
    }

}