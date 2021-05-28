package com.starfireaviation.rostermanagement.util;

import com.starfireaviation.rostermanagement.constants.RosterConstants;
import com.starfireaviation.rostermanagement.model.Country;
import com.starfireaviation.rostermanagement.model.Gender;
import com.starfireaviation.rostermanagement.model.MemberType;
import com.starfireaviation.rostermanagement.model.Person;
import com.starfireaviation.rostermanagement.model.State;
import com.starfireaviation.rostermanagement.model.Status;
import com.starfireaviation.rostermanagement.model.WebAdminAccess;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

public class WebRequestUtil {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(WebRequestUtil.class);

    /**
     * Date formatter.
     */
    private static final SimpleDateFormat MDY_SDF = new SimpleDateFormat("MM/dd/yyyy");

    /**
     * Performs login to EAA's roster management system.
     */
    public static void login(final HttpClient httpClient, final Map<String, String> headers) {
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
        } catch (Exception e) {
            LOGGER.error("[Login] Error", e);
        }
    }

    /**
     * Gets searchmembers page in EAA's roster management system.
     */
    public static Map<String, Element> viewSearchMembersPage(final HttpClient httpClient,
                                                             final Map<String, String> headers,
                                                             final Map<String, Element> viewStateMap) {
        final String uriStr = RosterConstants.EAA_CHAPTERS_SITE_BASE + "/searchmembers.aspx";
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .GET();
        for (final String key : headers.keySet()) {
            builder.setHeader(key, headers.get(key));
        }
        final HttpRequest request = builder.build();

        final Map<String, Element> map = new HashMap<>();
        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            final Document doc = Jsoup.parse(response.body());
            map.put(RosterConstants.VIEW_STATE, doc.getElementById(RosterConstants.VIEW_STATE));
            map.put(RosterConstants.VIEW_STATE_GENERATOR, doc.getElementById(RosterConstants.VIEW_STATE_GENERATOR));
            headers.put(RosterConstants.VIEW_STATE, getViewStateValue(viewStateMap.get(RosterConstants.VIEW_STATE)));
        } catch (Exception e) {
            LOGGER.error("[Search Page] Error", e);
        }
        return map;
    }

    /**
     * Checks if a user exists in EAA's roster management system.
     */
    public static boolean existsUser(final HttpClient httpClient,
                                     final Map<String, String> headers,
                                     final String firstName,
                                     final String lastName) {
        final String uriStr = RosterConstants.EAA_CHAPTERS_SITE_BASE + "/searchmembers.aspx";
        final String requestBodyStr = buildExistsUserRequestBodyString(headers, firstName, lastName);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyStr));
        headers.remove(RosterConstants.VIEW_STATE);
        headers.put(RosterConstants.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        for (final String key : headers.keySet()) {
            builder.setHeader(key, headers.get(key));
        }
        final HttpRequest request = builder.build();

        final StringBuilder sb = new StringBuilder();
        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            sb.append(response.body());
        } catch (Exception e) {
            LOGGER.error("[FETCH] Error", e);
        }
        return sb.toString().contains("lnkViewUpdateMember");
    }

    /**
     * Fetch's data from EAA's roster management system.
     */
    public static String fetchData(final HttpClient httpClient, final Map<String, String> headers) {
        final String uriStr = RosterConstants.EAA_CHAPTERS_SITE_BASE + "/searchmembers.aspx";
        final String requestBodyStr = buildFetchDataRequestBodyString(headers);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyStr));
        headers.remove(RosterConstants.VIEW_STATE);
        headers.put(RosterConstants.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        for (final String key : headers.keySet()) {
            builder.setHeader(key, headers.get(key));
        }
        final HttpRequest request = builder.build();

        final StringBuilder sb = new StringBuilder();
        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            sb.append(response.body());
        } catch (Exception e) {
            LOGGER.error("[FETCH] Error", e);
        }
        return sb.toString();
    }

    public static Map<String, String> getHttpHeaders(
            final HttpClient httpClient, final Map<String, Element> viewStateMap,
            final String username, final String password) {
        final Map<String, String> headers = new HashMap<>();
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(RosterConstants.EAA_CHAPTERS_SITE_BASE + "/main.aspx"))
                .GET()
                .build();
        try {
            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            final HttpHeaders responseHeaders = response.headers();
            final String cookieStr = responseHeaders.firstValue("set-cookie").orElse("");
            headers.put("cookie", cookieStr.substring(0, cookieStr.indexOf(";")));
        } catch (Exception e) {
            LOGGER.error("Error", e);
        }
        headers.put(RosterConstants.EVENT_TARGET, "");
        headers.put(RosterConstants.EVENT_ARGUMENT, "");
        headers.put(RosterConstants.VIEW_STATE, getViewStateValue(viewStateMap.get(RosterConstants.VIEW_STATE)));
        headers.put(RosterConstants.VIEW_STATE_GENERATOR,
                getViewStateGeneratorValue(viewStateMap.get(RosterConstants.VIEW_STATE_GENERATOR)));
        headers.put(RosterConstants.EVENT_VALIDATION, "/wEdAAaUkhCi8bB8A8YPK1mx/fN+Ob9NwfdsH6h5T4oBt2E/NC/PSAvxybIG70Gi7lMSo2Ha9mxIS56towErq28lcj7mn+o6oHBHkC8q81Z+42F7hK13DHQbwWPwDXbrtkgbgsBJaWfipkuZE5/MRRQAXrNwOiJp3YGlq4qKyVLK8XZVxQ==");
        headers.put(RosterConstants.USERNAME, username);
        headers.put(RosterConstants.PASSWORD, password);
        headers.put(RosterConstants.BUTTON, "Submit");
        headers.put(RosterConstants.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36");
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

    private static String buildLoginRequestBodyString(final Map<String, String> headers) {
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

    private static String buildFetchDataRequestBodyString(final Map<String, String> headers) {
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

    private static String buildExistsUserRequestBodyString(final Map<String, String> headers, final String firstName, final String lastName) {
        final StringBuilder sb = new StringBuilder();
        final List<String> data = new ArrayList<>();
        data.add(RosterConstants.EVENT_TARGET);
        data.add(RosterConstants.EVENT_ARGUMENT);
        data.add(RosterConstants.VIEW_STATE);
        data.add(RosterConstants.VIEW_STATE_GENERATOR);
        data.add(RosterConstants.VIEW_STATE_ENCRYPTED);
        data.add(RosterConstants.SEARCH_BUTTON);
        data.add(RosterConstants.FIRST_NAME + "=" + firstName);
        data.add(RosterConstants.LAST_NAME + "=" + lastName);
        data.add(RosterConstants.STATUS + "=Active");
        data.add(RosterConstants.SEARCH_MEMBER_TYPE);
        data.add(RosterConstants.CURRENT_STATUS);
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

    public static String buildNewUserRequestBodyString(final Map<String, Element> viewStateMap, final Person person) {
        String requestBody = RosterConstants.EVENT_TARGET + RosterConstants.EQUALS +
                RosterConstants.AMPERSAND + RosterConstants.EVENT_ARGUMENT + RosterConstants.EQUALS +
                RosterConstants.AMPERSAND + RosterConstants.LAST_FOCUS + RosterConstants.EQUALS +
                RosterConstants.AMPERSAND + RosterConstants.VIEW_STATE + RosterConstants.EQUALS +
                getViewStateValue(viewStateMap.get(RosterConstants.VIEW_STATE)) +
                RosterConstants.AMPERSAND + RosterConstants.VIEW_STATE_GENERATOR + RosterConstants.EQUALS +
                getViewStateGeneratorValue(viewStateMap.get(RosterConstants.VIEW_STATE_GENERATOR)) +
                RosterConstants.AMPERSAND + RosterConstants.VIEW_STATE_ENCRYPTED + RosterConstants.EQUALS +
                RosterConstants.AMPERSAND + RosterConstants.FIRST_NAME + RosterConstants.EQUALS +
                RosterConstants.AMPERSAND + RosterConstants.LAST_NAME + RosterConstants.EQUALS + person.getLastName() +
                RosterConstants.AMPERSAND + RosterConstants.STATUS + RosterConstants.EQUALS + person.getStatus() +
                RosterConstants.AMPERSAND + RosterConstants.SEARCH_MEMBER_TYPE + RosterConstants.EQUALS +
                RosterConstants.AMPERSAND + RosterConstants.CURRENT_STATUS + RosterConstants.EQUALS +
                RosterConstants.AMPERSAND + RosterConstants.ADD_NEW_MEMBER_BUTTON + RosterConstants.EQUALS + "Add+This+Person" +
                RosterConstants.AMPERSAND + RosterConstants.TEXT_FIRST_NAME + RosterConstants.EQUALS + person.getFirstName() +
                RosterConstants.AMPERSAND + RosterConstants.TEXT_LAST_NAME + RosterConstants.EQUALS + person.getLastName() +
                RosterConstants.AMPERSAND + RosterConstants.TEXT_NICK_NAME + RosterConstants.EQUALS + person.getNickname() +
                RosterConstants.AMPERSAND + RosterConstants.SPOUSE + RosterConstants.EQUALS + person.getSpouse() +
                RosterConstants.AMPERSAND + RosterConstants.GENDER + RosterConstants.EQUALS + person.getGender() +
                RosterConstants.AMPERSAND + RosterConstants.MEMBER_ID + RosterConstants.EQUALS + person.getEaaNumber() +
                RosterConstants.AMPERSAND + RosterConstants.MEMBER_TYPE + RosterConstants.EQUALS + person.getMemberType() +
                RosterConstants.AMPERSAND + RosterConstants.CURRENT_STANDING + RosterConstants.EQUALS + person.getStatus() +
                RosterConstants.AMPERSAND + RosterConstants.ADMIN_LEVEL + RosterConstants.EQUALS + person.getWebAdminAccess() +
                RosterConstants.AMPERSAND + RosterConstants.ADDRESS_LINE_1 + RosterConstants.EQUALS + person.getAddressLine1() +
                RosterConstants.AMPERSAND + RosterConstants.ADDRESS_LINE_2 + RosterConstants.EQUALS + person.getAddressLine2() +
                RosterConstants.AMPERSAND + RosterConstants.CITY + RosterConstants.EQUALS + person.getCity() +
                RosterConstants.AMPERSAND + RosterConstants.STATE + RosterConstants.EQUALS + person.getState() +
                RosterConstants.AMPERSAND + RosterConstants.ZIP_CODE + RosterConstants.EQUALS + person.getZipCode() +
                RosterConstants.AMPERSAND + RosterConstants.COUNTRY + RosterConstants.EQUALS + person.getCountry() +
                RosterConstants.AMPERSAND + RosterConstants.BIRTH_DATE + RosterConstants.EQUALS + person.getBirthDate() +
                RosterConstants.AMPERSAND + RosterConstants.JOIN_DATE + RosterConstants.EQUALS + person.getJoined() +
                RosterConstants.AMPERSAND + RosterConstants.EXPIRATION_DATE + RosterConstants.EQUALS + person.getExpiration() +
                RosterConstants.AMPERSAND + RosterConstants.OTHER_INFO + RosterConstants.EQUALS + person.getOtherInfo() +
                RosterConstants.AMPERSAND + RosterConstants.HOME_PHONE + RosterConstants.EQUALS + person.getHomePhone() +
                RosterConstants.AMPERSAND + RosterConstants.CELL_PHONE + RosterConstants.EQUALS + person.getCellPhone() +
                RosterConstants.AMPERSAND + RosterConstants.EMAIL + RosterConstants.EQUALS + person.getEmail() +
                RosterConstants.AMPERSAND + RosterConstants.RATINGS + RosterConstants.EQUALS + person.getRatings() +
                RosterConstants.AMPERSAND + RosterConstants.AIRCRAFT_OWNED + RosterConstants.EQUALS + person.getAircraftOwned() +
                RosterConstants.AMPERSAND + RosterConstants.AIRCRAFT_PROJECT + RosterConstants.EQUALS + person.getAircraftProject() +
                RosterConstants.AMPERSAND + RosterConstants.AIRCRAFT_BUILT + RosterConstants.EQUALS + person.getAircraftBuilt() +
                RosterConstants.AMPERSAND + RosterConstants.ROW_COUNT + RosterConstants.EQUALS + "50";
        return URLEncoder.encode(requestBody, StandardCharsets.UTF_8);
    }

    public static String buildUpdateUserRequestBodyString(final Map<String, Element> viewStateMap, final Person person) {
        final StringBuilder sb = new StringBuilder();
        addFormContent(sb, RosterConstants.EVENT_TARGET, RosterConstants.EMPTY_STRING);
        addFormContent(sb, RosterConstants.EVENT_ARGUMENT, RosterConstants.EMPTY_STRING);
        addFormContent(sb, RosterConstants.LAST_FOCUS, RosterConstants.EMPTY_STRING);
        addFormContent(sb, RosterConstants.VIEW_STATE, getViewStateValue(viewStateMap.get(RosterConstants.VIEW_STATE))
                .replaceAll("/", "%2F")
                .replaceAll("=", "%3D")
                .replaceAll("\\+", "%2B"));
        addFormContent(sb, RosterConstants.VIEW_STATE_GENERATOR,
                getViewStateGeneratorValue(viewStateMap.get(RosterConstants.VIEW_STATE_GENERATOR)));
        addFormContent(sb, RosterConstants.VIEW_STATE_ENCRYPTED, RosterConstants.EMPTY_STRING);
        addFormContent(sb, RosterConstants.FIRST_NAME, RosterConstants.EMPTY_STRING);
        addFormContent(sb, RosterConstants.LAST_NAME, person.getLastName());
        if (person.getStatus() != null) {
            addFormContent(sb, RosterConstants.STATUS, person.getStatus().toString());
        } else {
            addFormContent(sb, RosterConstants.STATUS, Status.INACTIVE.toString());
        }
        addFormContent(sb, RosterConstants.SEARCH_MEMBER_TYPE, RosterConstants.EMPTY_STRING);
        addFormContent(sb, RosterConstants.CURRENT_STATUS, RosterConstants.EMPTY_STRING);
        addFormContent(sb, RosterConstants.UPDATE_THIS_MEMBER_BUTTON, "Update");
        addFormContent(sb, RosterConstants.TEXT_FIRST_NAME, person.getFirstName());
        addFormContent(sb, RosterConstants.TEXT_LAST_NAME, person.getLastName());
        addFormContent(sb, RosterConstants.TEXT_NICK_NAME, person.getNickname());
        addFormContent(sb, RosterConstants.SPOUSE, person.getSpouse());
        addFormContent(sb, RosterConstants.GENDER, Gender.getDisplayString(person.getGender()));
        addFormContent(sb, RosterConstants.MEMBER_ID, person.getEaaNumber());
        addFormContent(sb, RosterConstants.MEMBER_TYPE, MemberType.toDisplayString(person.getMemberType()));
        addFormContent(sb, RosterConstants.CURRENT_STANDING, Status.getDisplayString(person.getStatus()));
        addFormContent(sb, RosterConstants.USER_NAME, person.getUsername());
        addFormContent(sb, RosterConstants.ADMIN_LEVEL, WebAdminAccess.getDisplayString(person.getWebAdminAccess()));
        addFormContent(sb, RosterConstants.ADDRESS_LINE_1, person.getAddressLine1());
        addFormContent(sb, RosterConstants.ADDRESS_LINE_2, person.getAddressLine2());
        addFormContent(sb, RosterConstants.CITY, person.getCity());
        addFormContent(sb, RosterConstants.STATE, State.getDisplayString(person.getState()));
        addFormContent(sb, RosterConstants.ZIP_CODE, person.getZipCode());
        addFormContent(sb, RosterConstants.COUNTRY, Country.toDisplayString(person.getCountry()));
        addFormContent(sb, RosterConstants.BIRTH_DATE, MDY_SDF.format(person.getBirthDateAsDate()));
        addFormContent(sb, RosterConstants.JOIN_DATE, MDY_SDF.format(person.getJoinedAsDate()));
        if (person.getExpiration() != null) {
            addFormContent(sb, RosterConstants.EXPIRATION_DATE, MDY_SDF.format(person.getExpiration()));
        } else {
            // TODO do something
        }
        addFormContent(sb, RosterConstants.OTHER_INFO, person.getOtherInfo());
        addFormContent(sb, RosterConstants.HOME_PHONE, person.getHomePhone());
        addFormContent(sb, RosterConstants.CELL_PHONE, person.getCellPhone());
        addFormContent(sb, RosterConstants.EMAIL, person.getEmail());
        addFormContent(sb, RosterConstants.RATINGS, person.getRatings());
        addFormContent(sb, RosterConstants.AIRCRAFT_OWNED, person.getAircraftOwned());
        addFormContent(sb, RosterConstants.AIRCRAFT_PROJECT, person.getAircraftProject());
        addFormContent(sb, RosterConstants.AIRCRAFT_BUILT, person.getAircraftBuilt());
        addFormContent(sb, RosterConstants.IMC, person.isImcClub() ? "on" : "off");
        addFormContent(sb, RosterConstants.VMC, person.isVmcClub() ? "on" : "off");
        addFormContent(sb, RosterConstants.YOUNG_EAGLE_PILOT, person.isYePilot() ? "on" : "off");
        addFormContent(sb, RosterConstants.EAGLE_PILOT, person.isEaglePilot() ? "on" : "off");
        sb
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.PHOTO)
                .append("\"; filename=\"\"\n")
                .append("Content-Type: application/octet-stream\n\n");
        addFormContent(sb, RosterConstants.PHOTO_FILE_NAME, RosterConstants.EMPTY_STRING);
        addFormContent(sb, RosterConstants.PHOTO_FILE_TYPE, RosterConstants.EMPTY_STRING);
        addFormContent(sb, RosterConstants.ROW_COUNT, "50");
        sb
                .append(RosterConstants.FORM_BOUNDARY)
                .append("--");
        return sb.toString();
    }

    /**
     * Adds a form content section to the provided StringBuilder object.
     */
    private static void addFormContent(final StringBuilder sb, final String key, final String value) {
        sb
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(key)
                .append(RosterConstants.FORM_DATA_SEPARATOR_DOUBLE_NL)
                .append(value);
    }

    private static String getViewStateValue(final Element viewState) {
        if (viewState != null) {
            return viewState.attr("value");
        }
        return "/wEPDwUKMTY1NDU2MTA1MmRkuOlmdf9IlE5Upbw3feS5bMlNeitv2Tys6h3WSL105GQ=";
    }

    private static String getViewStateGeneratorValue(final Element viewStateGenerator) {
        if (viewStateGenerator != null) {
            return viewStateGenerator.attr("value");
        }
        return "55FE2EBC";
    }
}
