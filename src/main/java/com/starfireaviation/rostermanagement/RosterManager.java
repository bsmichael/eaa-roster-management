package com.starfireaviation.rostermanagement;

import com.starfireaviation.rostermanagement.constants.RosterConstants;
import com.starfireaviation.rostermanagement.model.Country;
import com.starfireaviation.rostermanagement.model.Gender;
import com.starfireaviation.rostermanagement.model.MemberType;
import com.starfireaviation.rostermanagement.model.OtherInfo;
import com.starfireaviation.rostermanagement.model.Person;
import com.starfireaviation.rostermanagement.model.State;
import com.starfireaviation.rostermanagement.model.Status;
import com.starfireaviation.rostermanagement.model.WebAdminAccess;
import com.starfireaviation.rostermanagement.util.WebRequestUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RosterManager {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(RosterManager.class);

    /**
     * Date formatter.
     */
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * HttpClient.
     */
    private HttpClient httpClient;

    /**
     * Sets HttpClient.
     * Note: mostly used for unit test mocks
     *
     * @param value HttpClient
     */
    public void setHttpClient(final HttpClient value) {
        httpClient = value;
    }

    /**
     * Username to be used when interacting with eaachapters.org.
     */
    private String username = null;

    /**
     * Password to be used when interacting with eaachapters.org.
     */
    private String password = null;

    /**
     * List of Slack usernames.
     */
    private List<String> slackUsers = new ArrayList<>();

    /**
     * Initializes a RosterManager instance.
     *
     * @param username Username to be used when interacting with eaachapters.org
     * @param password Password to be used when interacting with eaachapters.org
     */
    public RosterManager(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Sets list of Slack user names.
     *
     * @param slackUsers list of Slack user names
     */
    public void setSlackUsers(final List<String> slackUsers) {
        if (slackUsers != null) {
            this.slackUsers = slackUsers;
        }
    }

    /**
     * Retrieves list of Slack user names.
     *
     * @return list of Slack user names
     */
     public List<String> getSlackUsers() {
         return slackUsers;
    }

    /**
     * Retrieves a list of all members.
     *
     * @return list of all members
     */
    public List<Person> getAllEntries() {
        Map<String, Element> viewStateMap = new HashMap<>();
        final Map<String, String> headers = WebRequestUtil.getHttpHeaders(httpClient, viewStateMap, username, password);
        WebRequestUtil.login(httpClient, headers);
        viewStateMap = WebRequestUtil.viewSearchMembersPage(httpClient, headers, viewStateMap);
        return parseRecords(headers);
    }

    /**
     * Create a new member entry.
     *
     * @param person to be created
     * @return new member information
     */
    public Person createEntry(final Person person) {
        Map<String, Element> viewStateMap = new HashMap<>();
        final Map<String, String> headers = WebRequestUtil.getHttpHeaders(httpClient, viewStateMap, username, password);
        WebRequestUtil.login(httpClient, headers);
        viewStateMap = WebRequestUtil.viewSearchMembersPage(httpClient, headers, viewStateMap);
        if (!WebRequestUtil.existsUser(httpClient, headers, person.getFirstName(), person.getLastName())) {
            // TODO: create user
            WebRequestUtil.buildNewUserRequestBodyString(viewStateMap, person);
            LOGGER.info("Creating new entry");
        }
        return person;
    }

    public Person updateEntry(final Person person) {
        Map<String, Element> viewStateMap = new HashMap<>();
        final Map<String, String> headers = WebRequestUtil.getHttpHeaders(httpClient, viewStateMap, username, password);
        WebRequestUtil.login(httpClient, headers);
        WebRequestUtil.viewSearchMembersPage(httpClient, headers, viewStateMap);
        if (WebRequestUtil.existsUser(httpClient, headers, person.getFirstName(), person.getLastName())) {
            // TODO: update user
            WebRequestUtil.buildUpdateUserRequestBodyString(viewStateMap, person);
            LOGGER.info("Updating existing entry");
        }
        return person;
    }

    /**
     * Parses select values from Excel spreadsheet.
     *
     * @return list of parsed values
     */
    private List<Person> parseRecords(final Map<String, String> headers) {
        final List<Person> records = new ArrayList<>();
        final Document doc = Jsoup.parse(WebRequestUtil.fetchData(httpClient, headers));
        final Elements tableRecords = doc.getElementsByTag("tr");
        int rowCount = 0;
        for (Element tr : tableRecords) {
            if (rowCount > 0) {
                try {
                    final Elements columns = tr.getElementsByTag("td");
                    int columnCount = 0;
                    final Person person = new Person();
                    for (Element column : columns) {
                        switch (columnCount) {
                            case 0:
                                person.setRosterId(Long.parseLong(column.text().trim()));
                                break;
                            case 1:
                                person.setMemberType(MemberType.valueOf(column.text().trim().replaceAll("-", "")));
                                break;
                            case 2:
                                person.setNickname(column.text().trim());
                                break;
                            case 3:
                                person.setFirstName(column.text().trim());
                                break;
                            case 4:
                                person.setLastName(column.text().trim());
                                break;
                            case 5:
                                person.setSpouse(column.text().trim());
                                break;
                            case 6:
                                person.setGender(Gender.fromDisplayString(column.text().trim().toUpperCase()));
                                break;
                            case 7:
                                person.setEmail(column.text().trim());
                                break;
                            case 8:
                                // Ignore EmailPrivate
                                break;
                            case 9:
                                person.setUsername(column.text().trim());
                                break;
                            case 10:
                                person.setBirthDate(column.text().trim());
                                break;
                            case 11:
                                person.setAddressLine1(column.text().trim());
                                break;
                            case 12:
                                person.setAddressLine2(column.text().trim());
                                break;
                            case 13:
                                // Ignore AddressPrivate
                                break;
                            case 14:
                                person.setHomePhone(column
                                        .text()
                                        .trim()
                                        .replaceAll(" ", "")
                                        .replaceAll("-", "")
                                        .replaceAll("\\(", "")
                                        .replaceAll("\\)", ""));
                                break;
                            case 15:
                                // Ignore HomePhonePrivate
                                break;
                            case 16:
                                person.setCellPhone(column
                                        .text()
                                        .trim()
                                        .replaceAll(" ", "")
                                        .replaceAll("-", "")
                                        .replaceAll("\\(", "")
                                        .replaceAll("\\)", ""));
                                break;
                            case 17:
                                // Ignore CellPhonePrivate
                                break;
                            case 18:
                                person.setEaaNumber(column.text().trim());
                                break;
                            case 19:
                                person.setStatus(Status.valueOf(column.text().trim().toUpperCase()));
                                break;
                            case 20:
                                person.setJoined(column.text().trim());
                                break;
                            case 21:
                                person.setExpiration(SDF.parse(column.text().trim()));
                                break;
                            case 22:
                                final OtherInfo otherInfo = new OtherInfo(column.text().trim());
                                person.setRfid(otherInfo.getRfid());
                                person.setSlack(otherInfo.getSlack());
                                person.setOtherInfo(otherInfo.getRaw());
                                person.setAdditionalInfo(otherInfo.getDescription());
                                if (otherInfo.getFamily() != null) {
                                    person.setFamily(String.join(", ", otherInfo.getFamily()));
                                }
                                if (person.getSlack() == null || "NULL".equalsIgnoreCase(person.getSlack())) {
                                    setSlack(slackUsers, person);
                                }
                                if (otherInfo.getNumOfFamily() != null) {
                                    person.setNumOfFamily(otherInfo.getNumOfFamily());
                                }
                                break;
                            case 23:
                                person.setCity(column.text().trim());
                                break;
                            case 24:
                                person.setState(State.fromDisplayString(column.text().trim()));
                                break;
                            case 25:
                                person.setCountry(Country.fromDisplayString(column.text().trim()));
                                break;
                            case 26:
                                person.setZipCode(column.text().trim());
                                break;
                            case 27:
                                person.setRatings(column.text().trim());
                                break;
                            case 28:
                                person.setAircraftOwned(column.text().trim());
                                break;
                            case 29:
                                person.setAircraftProject(column.text().trim());
                                break;
                            case 30:
                                person.setAircraftBuilt(column.text().trim());
                                break;
                            case 31:
                                person.setImcClub("yes".equalsIgnoreCase(column.text().trim()) ?
                                        Boolean.TRUE : Boolean.FALSE);
                                break;
                            case 32:
                                person.setVmcClub("yes".equalsIgnoreCase(column.text().trim()) ?
                                        Boolean.TRUE : Boolean.FALSE);
                                break;
                            case 33:
                                person.setYePilot("yes".equalsIgnoreCase(column.text().trim()) ?
                                        Boolean.TRUE : Boolean.FALSE);
                                break;
                            case 34:
                                person.setYeVolunteer("yes".equalsIgnoreCase(column.text().trim()) ?
                                        Boolean.TRUE : Boolean.FALSE);
                                break;
                            case 35:
                                person.setEaglePilot("yes".equalsIgnoreCase(column.text().trim()) ?
                                        Boolean.TRUE : Boolean.FALSE);
                                break;
                            case 36:
                                person.setEagleVolunteer("yes".equalsIgnoreCase(column.text().trim()) ?
                                        Boolean.TRUE : Boolean.FALSE);
                                break;
                            case 37:
                                // Ignore DateAdded
                                break;
                            case 38:
                                // Ignore DateUpdated
                                break;
                            case 39:
                                person.setEaaExpiration(column.text().trim());
                                break;
                            case 40:
                                person.setYouthProtection(column.text().trim());
                                break;
                            case 41:
                                person.setBackgroundCheck(column.text().trim());
                                break;
                            case 42:
                                // Ignore UpdatedBy
                                break;
                            case 43:
                                person.setWebAdminAccess(WebAdminAccess.fromDisplayString(column.text().trim()));
                                break;
                            default:
                                // Do nothing
                        }
                        columnCount++;
                    }
                    records.add(person);
                } catch (Exception e) {
                    LOGGER.error("Error", e);
                }
            }
            rowCount++;
        }
        return records;
    }

    /**
     * Assigns slack username if not already assigned and a first/last name match is found.
     *
     * @param slackUsers list of all Slack users
     * @param person Member
     */
    private void setSlack(final List<String> slackUsers, final Person person) {
        final String username = person.getFirstName() + " " + person.getLastName();
        slackUsers.forEach(str -> {
            final String split[] = str.split("\\|");
            if (!"NULL".equalsIgnoreCase(split[1]) && str.contains(username)) {
                person.setSlack(split[1]);
            }
        });
    }

}
