package io.github.bsmichael.rostermanagement.service;

import io.github.bsmichael.rostermanagement.constants.RosterConstants;
import io.github.bsmichael.rostermanagement.model.Country;
import io.github.bsmichael.rostermanagement.model.Gender;
import io.github.bsmichael.rostermanagement.model.MemberType;
import io.github.bsmichael.rostermanagement.model.OtherInfo;
import io.github.bsmichael.rostermanagement.model.Person;
import io.github.bsmichael.rostermanagement.model.State;
import io.github.bsmichael.rostermanagement.model.Status;
import io.github.bsmichael.rostermanagement.model.WebAdminAccess;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RosterManagerService {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(RosterManagerService.class);

    /**
     * Date representing the beginning of dates.
     */
    private final static Date ZERO_DATE = new Date(0);

    /**
     * Date formatter.
     */
    private static final SimpleDateFormat MDY_SDF = new SimpleDateFormat("MM/dd/yyyy");

    /**
     * Date formatter.
     */
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    private final Pattern viewStatePattern =
            Pattern.compile(".*<input type=\"hidden\" name=\"__VIEWSTATE\" id=\"__VIEWSTATE\" value=\"(.*?)\" />.*",
                    Pattern.DOTALL);

    private final Map<String, Element> viewStateMap = new HashMap<>();

    /**
     * List of Slack usernames.
     */
    private List<String> slackUsers = new ArrayList<>();

    /**
     * Tracks login status.
     */
    private boolean loggedIn = false;

    public RosterManagerService() {
        // Do nothing
    }

    /**
     * Sets list of Slack user names.
     *
     * @param slackUsers list of Slack user names
     */
    public void setSlackUsers(final List<String> slackUsers) {
        if (slackUsers != null) {
            this.slackUsers = slackUsers;
            LOGGER.info("slackUsers set");
        }
    }

    /**
     * Performs login to EAA's roster management system.
     */
    public void login(final HttpClient httpClient, final Map<String, String> headers) {
        LOGGER.debug("Performing login...");
        final String uriStr = RosterConstants.EAA_CHAPTERS_SITE_BASE + "/main.aspx";
        final String requestBodyStr = buildLoginRequestBodyString(headers);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyStr));
        for (final String key : headers.keySet()) {
            builder.setHeader(key, headers.get(key));
        }
        final HttpRequest request = builder.build();

        try {
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            loggedIn = true;
        } catch (Exception e) {
            LOGGER.error("[Login] Error", e);
        }
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void logout(final HttpClient httpClient, final Map<String, String> headers) {
        LOGGER.debug("Performing logout...");
        final String uriStr = RosterConstants.EAA_CHAPTERS_SITE_BASE + "/main.aspx";
        final String requestBodyStr = buildLogoutRequestBodyString(headers);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyStr));
        for (final String key : headers.keySet()) {
            builder.setHeader(key, headers.get(key));
        }
        final HttpRequest request = builder.build();
        try {
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            loggedIn = false;
        } catch (Exception e) {
            LOGGER.error("[Logout] Error", e);
        }
    }

    /**
     * Gets searchmembers page in EAA's roster management system.
     */
    public void viewSearchMembersPage(final HttpClient httpClient,
                                       final Map<String, String> headers) {
        final String uriStr = RosterConstants.EAA_CHAPTERS_SITE_BASE + "/searchmembers.aspx";
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .GET();
        for (final String key : headers.keySet()) {
            builder.setHeader(key, headers.get(key));
        }
        final HttpRequest request = builder.build();

        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            LOGGER.debug("Received status code=" + response.statusCode());

            final Document doc = Jsoup.parse(response.body());
            viewStateMap.put(RosterConstants.VIEW_STATE, doc.getElementById(RosterConstants.VIEW_STATE));
            viewStateMap.put(RosterConstants.VIEW_STATE_GENERATOR,
                    doc.getElementById(RosterConstants.VIEW_STATE_GENERATOR));
            headers.put(RosterConstants.VIEW_STATE, getViewStateValue(viewStateMap.get(RosterConstants.VIEW_STATE)));
        } catch (Exception e) {
            LOGGER.error("[Search Page] Error", e);
        }
    }

    /**
     * Gets searchmembers page in EAA's roster management system.
     */
    public void addMember(final HttpClient httpClient, final Map<String, String> headers,
                          final String viewState, final Person person) {
        try {
            final String uriStr = RosterConstants.EAA_CHAPTERS_SITE_BASE + "/searchmembers.aspx";
            final HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(uriStr))
                    .POST(buildNewUserRequestBodyString(person, viewState));
            headers.put(RosterConstants.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/"+
                    "webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            headers.put(RosterConstants.CONTENT_TYPE, "application/x-www-form-urlencoded");
            headers.put("sec-ch-ua", "\" Not;A Brand\";v=\"99\", \"Google Chrome\";v=\"91\", \"Chromium\";v=\"91\"");
            headers.put("sec-ch-ua-mobile", "?0");
            headers.put("upgrade-insecure-requests", "1");
            headers.put("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.164 Safari/537.36");
            headers.put("sec-fetch-site", "same-origin");
            headers.put("sec-fetch-mode", "navigate");
            headers.put("sec-fetch-user", "?1");
            headers.put("sec-fetch-dest", "document");
            //for (final String key : headers.keySet()) {
            //    builder.setHeader(key, headers.get(key));
            //}
            final HttpRequest request = builder.build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.info("Received status code=" + response.statusCode());
            LOGGER.info("Received headers=" + response.headers());
            LOGGER.info("[Add member] response: " + response.body());
        } catch (Exception e) {
            LOGGER.error("[Add Member] Error", e);
        }
    }

    public void updateMember(final HttpClient httpClient, final Map<String, String> headers, final Person person) {
        LOGGER.info("Updating existing entry");
    }

    /**
     * Checks if a user exists in EAA's roster management system.
     */
    public boolean existsUser(final HttpClient httpClient,
                               final Map<String, String> headers,
                               final String firstName,
                               final String lastName) {
        LOGGER.debug(String.format("Checking if %s %s exists...", firstName, lastName));
        final String uriStr = RosterConstants.EAA_CHAPTERS_SITE_BASE + "/searchmembers.aspx";
        final String requestBodyStr = buildExistsUserRequestBodyString(headers, firstName, lastName);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyStr));
        headers.remove(RosterConstants.VIEW_STATE);
        headers.put(RosterConstants.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/"+
                "webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        for (final String key : headers.keySet()) {
            builder.setHeader(key, headers.get(key));
        }
        final HttpRequest request = builder.build();

        final StringBuilder sb = new StringBuilder();
        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            LOGGER.debug("Received status code=" + response.statusCode());
            sb.append(response.body());
        } catch (Exception e) {
            LOGGER.error("[existsUser] Error", e);
        }
        return sb.toString().contains("lnkViewUpdateMember");
    }

    /**
     * Checks if a user exists in EAA's roster management system.
     */
    public String startAddUser(final HttpClient httpClient,
                              final Map<String, String> headers) {
        LOGGER.debug("Starting add user process...");
        final String uriStr = RosterConstants.EAA_CHAPTERS_SITE_BASE + "/searchmembers.aspx";
        final String requestBodyStr = buildAddUserRequestBodyString(headers);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyStr));
        headers.remove(RosterConstants.VIEW_STATE);
        headers.put(RosterConstants.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/"+
                "webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        for (final String key : headers.keySet()) {
            builder.setHeader(key, headers.get(key));
        }

        try {
            final HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            LOGGER.debug("Received status code=" + response.statusCode());
            LOGGER.debug("Received headers: " + response.headers());
            final Matcher m = viewStatePattern.matcher(response.body());
            if (m.matches()) {
                return m.group(1);
            }
        } catch (Exception e) {
            LOGGER.error("[startAddUser] Error", e);
        }
        return null;
    }

    /**
     * Fetch's data from EAA's roster management system.
     */
    public String fetchData(final HttpClient httpClient, final Map<String, String> headers) {
        final String uriStr = RosterConstants.EAA_CHAPTERS_SITE_BASE + "/searchmembers.aspx";
        final String requestBodyStr = buildFetchDataRequestBodyString(headers);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyStr));
        headers.remove(RosterConstants.VIEW_STATE);
        headers.put(RosterConstants.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/"+
                "webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        for (final String key : headers.keySet()) {
            builder.setHeader(key, headers.get(key));
        }
        final HttpRequest request = builder.build();

        final StringBuilder sb = new StringBuilder();
        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            LOGGER.info("Received status code=" + response.statusCode());
            sb.append(response.body());
        } catch (Exception e) {
            LOGGER.error("[fetchData] Error", e);
        }
        return sb.toString();
    }

    public Map<String, String> getHttpHeaders(
            final HttpClient httpClient, final String username, final String password) {
        final Map<String, String> headers = new HashMap<>();
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(RosterConstants.EAA_CHAPTERS_SITE_BASE + "/main.aspx"))
                .GET()
                .build();
        try {
            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.debug("Received status code=" + response.statusCode());
            final HttpHeaders responseHeaders = response.headers();
            final String cookieStr = responseHeaders.firstValue("set-cookie").orElse("");
            headers.put("cookie", cookieStr.substring(0, cookieStr.indexOf(";")));
        } catch (Exception e) {
            LOGGER.error("[getHttpHeaders] Error", e);
        }
        headers.put(RosterConstants.EVENT_TARGET, "");
        headers.put(RosterConstants.EVENT_ARGUMENT, "");
        headers.put(RosterConstants.VIEW_STATE, getViewStateValue(viewStateMap.get(RosterConstants.VIEW_STATE)));
        headers.put(RosterConstants.VIEW_STATE_GENERATOR,
                getViewStateGeneratorValue(viewStateMap.get(RosterConstants.VIEW_STATE_GENERATOR)));
        headers.put(RosterConstants.EVENT_VALIDATION, "/wEdAAaUkhCi8bB8A8YPK1mx/fN+Ob9NwfdsH6h5T4oBt2E/NC/PSAvxybIG70G"+
                "i7lMSo2Ha9mxIS56towErq28lcj7mn+o6oHBHkC8q81Z+42F7hK13DHQbwWPwDXbrtkgbgsBJaWfipkuZE5/MRRQAXrNwOiJp3YGl"+
                "q4qKyVLK8XZVxQ==");
        headers.put(RosterConstants.USERNAME, username);
        headers.put(RosterConstants.PASSWORD, password);
        headers.put(RosterConstants.BUTTON, "Submit");
        headers.put(RosterConstants.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 "+
                "(KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36");
        headers.put(RosterConstants.CONTENT_TYPE, "application/x-www-form-urlencoded");
        headers.put(RosterConstants.EXPORT_BUTTON, "Results+To+Excel");
        headers.put(RosterConstants.STATUS, "Active");
        headers.put(RosterConstants.FIRST_NAME, "");
        headers.put(RosterConstants.LAST_NAME, "");
        headers.put(RosterConstants.SEARCH_MEMBER_TYPE, "");
        headers.put(RosterConstants.CURRENT_STATUS, "");
        headers.put(RosterConstants.ROW_COUNT, "");
        headers.put(RosterConstants.VIEW_STATE_ENCRYPTED, "");
        headers.put(RosterConstants.LAST_FOCUS, "");
        return headers;
    }

    private String buildLoginRequestBodyString(final Map<String, String> headers) {
        final StringBuilder sb = new StringBuilder();
        final List<String> data = new ArrayList<>();
        data.add(RosterConstants.EVENT_TARGET);
        data.add(RosterConstants.EVENT_ARGUMENT);
        data.add(RosterConstants.VIEW_STATE);
        data.add(RosterConstants.VIEW_STATE_GENERATOR);
        data.add(RosterConstants.EVENT_VALIDATION);
        data.add(RosterConstants.USERNAME);
        data.add(RosterConstants.PASSWORD);
        data.add(RosterConstants.BUTTON);
        for (final String key : headers.keySet()) {
            if (data.contains(key)) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                if (RosterConstants.USERNAME.equals(key) ||
                        RosterConstants.PASSWORD.equals(key) || RosterConstants.BUTTON.equals(key)) {
                    sb.append(key.replaceAll("\\$", "%24"));
                } else {
                    sb.append(key);
                }
                sb.append("=");
                if (RosterConstants.VIEW_STATE.equals(key) || RosterConstants.EVENT_VALIDATION.equals(key)) {
                    sb.append(headers.get(key)
                            .replaceAll("/", "%2F")
                            .replaceAll("=", "%3D")
                            .replaceAll("\\+", "%2B"));
                } else {
                    sb.append(headers.get(key));
                }
            }
        }
        return sb.toString();
    }

    private String buildLogoutRequestBodyString(Map<String, String> headers) {
        final StringBuilder sb = new StringBuilder();
        final List<String> data = new ArrayList<>();
        data.add(RosterConstants.EVENT_TARGET);
        data.add(RosterConstants.EVENT_ARGUMENT);
        data.add(RosterConstants.VIEW_STATE);
        data.add(RosterConstants.VIEW_STATE_GENERATOR);
        data.add(RosterConstants.EVENT_VALIDATION);
        for (final String key : headers.keySet()) {
            if (data.contains(key)) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(key);
                sb.append("=");
                if (RosterConstants.VIEW_STATE.equals(key) || RosterConstants.EVENT_VALIDATION.equals(key)) {
                    sb.append(headers.get(key)
                            .replaceAll("/", "%2F")
                            .replaceAll("=", "%3D")
                            .replaceAll("\\+", "%2B"));
                } else if (RosterConstants.EVENT_TARGET.equals(key)) {
                    sb.append("ctl00$lnkbtnLogoff");
                } else {
                    sb.append(headers.get(key));
                }
            }
        }
        return sb.toString();
    }

    private String buildFetchDataRequestBodyString(final Map<String, String> headers) {
        final StringBuilder sb = new StringBuilder();
        final List<String> data = new ArrayList<>();
        data.add(RosterConstants.EVENT_TARGET);
        data.add(RosterConstants.EVENT_ARGUMENT);
        data.add(RosterConstants.LAST_FOCUS);
        data.add(RosterConstants.VIEW_STATE);
        data.add(RosterConstants.VIEW_STATE_GENERATOR);
        data.add(RosterConstants.VIEW_STATE_ENCRYPTED);
        data.add(RosterConstants.FIRST_NAME);
        data.add(RosterConstants.LAST_NAME);
        data.add(RosterConstants.EXPORT_BUTTON);
        data.add(RosterConstants.STATUS);
        data.add(RosterConstants.SEARCH_MEMBER_TYPE);
        data.add(RosterConstants.CURRENT_STATUS);
        data.add(RosterConstants.ROW_COUNT);
        return addDataFromHeaders(headers, sb, data);
    }

    private String buildExistsUserRequestBodyString(final Map<String, String> headers, final String firstName,
                                                    final String lastName) {
        final StringBuilder sb = new StringBuilder();
        final List<String> data = new ArrayList<>();
        data.add(RosterConstants.EVENT_TARGET);
        data.add(RosterConstants.EVENT_ARGUMENT);
        data.add(RosterConstants.VIEW_STATE);
        data.add(RosterConstants.VIEW_STATE_GENERATOR);
        data.add(RosterConstants.VIEW_STATE_ENCRYPTED);
        data.add(RosterConstants.SEARCH_BUTTON);
        data.add(RosterConstants.FIRST_NAME + RosterConstants.EQUALS + firstName);
        data.add(RosterConstants.LAST_NAME + RosterConstants.EQUALS + lastName);
        data.add(RosterConstants.STATUS + "=Active");
        data.add(RosterConstants.SEARCH_MEMBER_TYPE);
        data.add(RosterConstants.CURRENT_STATUS);
        return addDataFromHeaders(headers, sb, data);
    }

    private String buildAddUserRequestBodyString(final Map<String, String> headers) {
        final StringBuilder sb = new StringBuilder();
        final List<String> data = new ArrayList<>();
        data.add(RosterConstants.EVENT_TARGET);
        data.add(RosterConstants.EVENT_ARGUMENT);
        data.add(RosterConstants.VIEW_STATE);
        data.add(RosterConstants.VIEW_STATE_GENERATOR);
        data.add(RosterConstants.VIEW_STATE_ENCRYPTED);
        data.add(RosterConstants.SHOW_MEMBER_ADD_PANEL_BUTTON + RosterConstants.EQUALS + "Add a new Person!");
        data.add(RosterConstants.STATUS + "=Active");
        data.add(RosterConstants.SEARCH_MEMBER_TYPE);
        data.add(RosterConstants.CURRENT_STATUS);
        return addDataFromHeaders(headers, sb, data);
    }

    private HttpRequest.BodyPublisher buildNewUserRequestBodyString(final Person person, final String viewState) {
        final Map<Object, Object> data = new HashMap<>();
        data.put(RosterConstants.EVENT_TARGET, RosterConstants.EMPTY_STRING);
        data.put(RosterConstants.EVENT_ARGUMENT, RosterConstants.EMPTY_STRING);
        data.put(RosterConstants.VIEW_STATE, viewState);
        data.put(RosterConstants.VIEW_STATE_GENERATOR,
                getViewStateGeneratorValue(viewStateMap.get(RosterConstants.VIEW_STATE_GENERATOR)));
        data.put(RosterConstants.VIEW_STATE_ENCRYPTED, RosterConstants.EMPTY_STRING);
        data.put(RosterConstants.FIRST_NAME, RosterConstants.EMPTY_STRING);
        data.put(RosterConstants.LAST_NAME, RosterConstants.EMPTY_STRING);
        data.put(RosterConstants.STATUS, Status.getDisplayString(person.getStatus()));
        data.put(RosterConstants.SEARCH_MEMBER_TYPE, RosterConstants.EMPTY_STRING);
        data.put(RosterConstants.CURRENT_STATUS, RosterConstants.EMPTY_STRING);
        data.put(RosterConstants.ADD_NEW_MEMBER_BUTTON, "Add This Person");
        // Required Field
        data.put(RosterConstants.TEXT_FIRST_NAME, person.getFirstName());
        // Required Field
        data.put(RosterConstants.TEXT_LAST_NAME, person.getLastName());
        if (person.getNickname() != null) {
            data.put(RosterConstants.TEXT_NICK_NAME, person.getNickname());
        } else {
            data.put(RosterConstants.TEXT_NICK_NAME, RosterConstants.EMPTY_STRING);
        }
        if (person.getSpouse() != null) {
            data.put(RosterConstants.SPOUSE, person.getSpouse());
        } else {
            data.put(RosterConstants.SPOUSE, RosterConstants.EMPTY_STRING);
        }
        data.put(RosterConstants.GENDER, Gender.getDisplayString(person.getGender()));
        // Numbers only
        if (person.getEaaNumber() != null) {
            data.put(RosterConstants.MEMBER_ID, person.getEaaNumber());
        } else {
            data.put(RosterConstants.MEMBER_ID, RosterConstants.EMPTY_STRING);
        }
        // Required Field
        data.put(RosterConstants.MEMBER_TYPE, MemberType.toDisplayString(person.getMemberType()));
        // Required Field
        data.put(RosterConstants.CURRENT_STANDING, Status.getDisplayString(person.getStatus()));
        // Required Field
        data.put(RosterConstants.ADMIN_LEVEL, WebAdminAccess.getDisplayString(person.getWebAdminAccess()));
        if (person.getAddressLine1() != null) {
            data.put(RosterConstants.ADDRESS_LINE_1, person.getAddressLine1());
        } else {
            data.put(RosterConstants.ADDRESS_LINE_1, RosterConstants.EMPTY_STRING);
        }
        if (person.getAddressLine2() != null) {
            data.put(RosterConstants.ADDRESS_LINE_2, person.getAddressLine2());
        } else {
            data.put(RosterConstants.ADDRESS_LINE_2, RosterConstants.EMPTY_STRING);
        }
        if (person.getCity() != null) {
            data.put(RosterConstants.CITY, person.getCity());
        } else {
            data.put(RosterConstants.CITY, RosterConstants.EMPTY_STRING);
        }
        data.put(RosterConstants.STATE, State.getDisplayString(person.getState()));
        if (person.getZipCode() != null) {
            data.put(RosterConstants.ZIP_CODE, person.getZipCode());
        } else {
            data.put(RosterConstants.ZIP_CODE, RosterConstants.EMPTY_STRING);
        }
        data.put(RosterConstants.COUNTRY, Country.toDisplayString(person.getCountry()));
        // Must be in mm/dd/yyyy format
        if (person.getBirthDate() != null) {
            data.put(RosterConstants.BIRTH_DATE, person.getBirthDate());
        } else {
            data.put(RosterConstants.BIRTH_DATE, RosterConstants.EMPTY_STRING);
        }
        // Must be in mm/dd/yyyy format
        if (person.getJoined() != null) {
            data.put(RosterConstants.JOIN_DATE, person.getJoined());
        } else {
            data.put(RosterConstants.JOIN_DATE, RosterConstants.EMPTY_STRING);
        }
        // Must be in mm/dd/yyyy format
        // Required Field
        data.put(RosterConstants.EXPIRATION_DATE, MDY_SDF.format(person.getExpiration()));
        if (person.getOtherInfo() != null) {
            data.put(RosterConstants.OTHER_INFO, person.getOtherInfo());
        } else {
            data.put(RosterConstants.OTHER_INFO, RosterConstants.EMPTY_STRING);
        }
        if (person.getHomePhone() != null) {
            data.put(RosterConstants.HOME_PHONE, person.getHomePhone()
                    .replaceAll("\\.", RosterConstants.EMPTY_STRING)
                    .replaceAll("-", RosterConstants.EMPTY_STRING)
                    .replaceAll("\\(", RosterConstants.EMPTY_STRING)
                    .replaceAll("\\)", RosterConstants.EMPTY_STRING));
        } else {
            data.put(RosterConstants.HOME_PHONE, RosterConstants.EMPTY_STRING);
        }
        if (person.getCellPhone() != null) {
            data.put(RosterConstants.CELL_PHONE, person.getCellPhone()
                    .replaceAll("\\.", RosterConstants.EMPTY_STRING)
                    .replaceAll("-", RosterConstants.EMPTY_STRING)
                    .replaceAll("\\(", RosterConstants.EMPTY_STRING)
                    .replaceAll("\\)", RosterConstants.EMPTY_STRING));
        } else {
            data.put(RosterConstants.CELL_PHONE, RosterConstants.EMPTY_STRING);
        }
        if (person.getEmail() != null) {
            data.put(RosterConstants.EMAIL, person.getEmail());
        } else {
            data.put(RosterConstants.EMAIL, RosterConstants.EMPTY_STRING);
        }
        if (person.getRatings() != null) {
            data.put(RosterConstants.RATINGS, person.getRatings());
        } else {
            data.put(RosterConstants.RATINGS, RosterConstants.EMPTY_STRING);
        }
        if (person.getAircraftOwned() != null) {
            data.put(RosterConstants.AIRCRAFT_OWNED, person.getAircraftOwned());
        } else {
            data.put(RosterConstants.AIRCRAFT_OWNED, RosterConstants.EMPTY_STRING);
        }
        if (person.getAircraftProject() != null) {
            data.put(RosterConstants.AIRCRAFT_PROJECT, person.getAircraftProject());
        } else {
            data.put(RosterConstants.AIRCRAFT_PROJECT, RosterConstants.EMPTY_STRING);
        }
        if (person.getAircraftBuilt() != null) {
            data.put(RosterConstants.AIRCRAFT_BUILT, person.getAircraftBuilt());
        } else {
            data.put(RosterConstants.AIRCRAFT_BUILT, RosterConstants.EMPTY_STRING);
        }
        if (person.isImcClub()) {
            data.put(RosterConstants.IMC, "on");
        }
        if (person.isVmcClub()) {
            data.put(RosterConstants.VMC, "on");
        }
        if (person.isYePilot()) {
            data.put(RosterConstants.YOUNG_EAGLE_PILOT, "on");
        }
        if (person.isYeVolunteer()) {
            data.put(RosterConstants.YOUNG_EAGLE_VOLUNTEER, "on");
        }
        if (person.isEaglePilot()) {
            data.put(RosterConstants.EAGLE_PILOT, "on");
        }
        if (person.isEagleVolunteer()) {
            data.put(RosterConstants.EAGLE_FLIGHT_VOLUNTEER, "on");
        }
        data.put(RosterConstants.ROW_COUNT, "50");
        return ofFormData(data);
    }

    public static HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        final StringBuilder builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            final String key = URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8);
            final String value = URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8);
            //LOGGER.info("[ofFormData] appending " + key + RosterConstants.EQUALS + value);
            builder.append(key);
            builder.append(RosterConstants.EQUALS);
            builder.append(value);
        }
        final String dataStr = builder.toString();
        LOGGER.info("[ofFormData] built " + dataStr);
        return HttpRequest.BodyPublishers.ofString(dataStr);
    }

    private String addDataFromHeaders(Map<String, String> headers, StringBuilder sb, List<String> data) {
        for (final String key : headers.keySet()) {
            if (data.contains(key)) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                if (RosterConstants.FIRST_NAME.equals(key) ||
                        RosterConstants.LAST_NAME.equals(key) ||
                        RosterConstants.EXPORT_BUTTON.equals(key) ||
                        RosterConstants.STATUS.equals(key) ||
                        RosterConstants.SEARCH_MEMBER_TYPE.equals(key) ||
                        RosterConstants.CURRENT_STATUS.equals(key) ||
                        RosterConstants.ROW_COUNT.equals(key)) {
                    sb.append(key.replaceAll("\\$", "%24"));
                } else {
                    sb.append(key);
                }
                sb.append("=");
                if (RosterConstants.VIEW_STATE.equals(key) || RosterConstants.EVENT_VALIDATION.equals(key)) {
                    sb.append(headers.get(key)
                            .replaceAll("/", "%2F")
                            .replaceAll("=", "%3D")
                            .replaceAll("\\+", "%2B"));
                } else {
                    sb.append(headers.get(key));
                }
            }
        }
        return sb.toString();
    }

    private String getViewStateValue(final Element viewState) {
        if (viewState != null) {
            return viewState.attr("value");
        }
        return "/wEPDwUKMTY1NDU2MTA1MmRkuOlmdf9IlE5Upbw3feS5bMlNeitv2Tys6h3WSL105GQ=";
    }

    private String getViewStateGeneratorValue(final Element viewStateGenerator) {
        if (viewStateGenerator != null) {
            return viewStateGenerator.attr("value");
        }
        return "55FE2EBC";
    }

    /**
     * Parses select values from Excel spreadsheet.
     *
     * @return list of parsed values
     */
    public List<Person> parseRecords(final String data) {
        final List<Person> records = new ArrayList<>();
        final Document doc = Jsoup.parse(data);
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
            final String[] split = str.split("\\|");
            if (!"NULL".equalsIgnoreCase(split[1]) && str.contains(username)) {
                person.setSlack(split[1]);
            }
        });
    }

}
