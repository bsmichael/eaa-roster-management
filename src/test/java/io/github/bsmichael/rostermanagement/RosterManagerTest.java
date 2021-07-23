package io.github.bsmichael.rostermanagement;

import com.github.javafaker.Faker;
import io.github.bsmichael.rostermanagement.model.Country;
import io.github.bsmichael.rostermanagement.model.Gender;
import io.github.bsmichael.rostermanagement.model.MemberType;
import io.github.bsmichael.rostermanagement.model.Person;
import io.github.bsmichael.rostermanagement.model.State;
import io.github.bsmichael.rostermanagement.model.Status;
import io.github.bsmichael.rostermanagement.model.WebAdminAccess;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class RosterManagerTest {

    /**
     * HttpClient.
     */
    @Mock
    private HttpClient httpClient;

    /**
     * HttpResponse.
     */
    @Mock
    private HttpResponse httpResponse;

    /**
     * Object under test.
     */
    private RosterManager rosterManager = null;

    /**
     * Test username.
     */
    private final static String USERNAME = "USERNAME";

    /**
     * Test password.
     */
    private final static String PASSWORD = "PASSWORD";

    private String eaaMemberSearchFileContents = null;

    private Faker faker = new Faker(new Locale("en-US"));

    /**
     * Date formatter.
     */
    private static final SimpleDateFormat MDY_SDF = new SimpleDateFormat("MM/dd/yyyy");

    /**
     * Test setup.
     *
     * @throws Exception when a test error occurs
     */
    @Before
    public void before() throws Exception {
        final BufferedReader br = new BufferedReader(
                new InputStreamReader(this.getClass().getResourceAsStream("/EAAMemberSearch.html")));
        final StringBuilder sb = new StringBuilder();
        while (br.ready()) {
            sb.append(br.readLine());
        }
        eaaMemberSearchFileContents = sb.toString();

        MockitoAnnotations.openMocks(this);

        final Map<String, List<String>> headers = new HashMap<>();
        headers.put("set-cookie", Collections.singletonList("abc; 123"));
        HttpHeaders responseHeaders = HttpHeaders.of(headers, (x, y) -> x.equals(x));
        Mockito
                .when(httpClient.send(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(httpResponse);
        Mockito.when(httpResponse.headers()).thenReturn(responseHeaders);
        Mockito.doReturn("<html></html>").when(httpResponse).body();

        rosterManager = new RosterManager(USERNAME, PASSWORD, httpClient);
    }

    @Test
    public void testConstructor() {
        rosterManager = new RosterManager(USERNAME, PASSWORD);

        Assert.assertNotNull(rosterManager);
    }

    /**
     * Test setting of Slack users.
     *
     */
    @Test
    public void setSlackUsersTest() {
        rosterManager.setSlackUsers(new ArrayList<>());
    }

    /**
     * Test retrieval of all roster entries.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void getAllEntriesTest() throws Exception {
        Mockito.doReturn(eaaMemberSearchFileContents).when(httpResponse).body();

        final List<Person> people = rosterManager.getAllEntries();
        Assert.assertNotNull(people);

        Mockito.verify(httpClient, Mockito.times(5)).send(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(httpClient);
    }

    /**
     * Test adding of a person in the roster management system.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void savePersonTest() throws Exception {
        final Person createdPerson = rosterManager.savePerson(generatePerson());

        Assert.assertNotNull(createdPerson);
        Mockito.verify(httpClient, Mockito.times(6)).send(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(httpClient);
    }

    /**
     * Test adding of a person in the roster management system.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void savePersonMinimalTest() throws Exception {
        final Person createdPerson = rosterManager.savePerson(generateMinimalPerson());

        Assert.assertNotNull(createdPerson);
        Mockito.verify(httpClient, Mockito.times(6)).send(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(httpClient);
    }

    /**
     * Test adding of a person in the roster management system.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void savePersonFailedLoginTest() throws Exception {
        Mockito.doThrow(new IOException()).when(httpClient).send(ArgumentMatchers.any(), ArgumentMatchers.any());
        final Person createdPerson = rosterManager.savePerson(generatePerson());

        Assert.assertNotNull(createdPerson);
        Mockito.verify(httpClient, Mockito.times(5)).send(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(httpClient);
    }

    /**
     * Test adding of a person in the roster management system.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void savePersonExistsTest() throws Exception {
        Mockito.doReturn("<html>lnkViewUpdateMember</html>").when(httpResponse).body();

        final Person createdPerson = rosterManager.savePerson(generatePerson());

        Assert.assertNotNull(createdPerson);
        Mockito.verify(httpClient, Mockito.times(5)).send(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(httpClient);
    }

    /**
     * Test deleting a person from the roster management system.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void deletePersonTest() throws Exception {
        Assert.assertTrue(rosterManager.deletePerson(1L));

        // TODO mockito tests
    }

    /**
     * Test deleting a person from the roster management system.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void deletePersonNullIDTest() throws Exception {
        Assert.assertFalse(rosterManager.deletePerson(null));

        // TODO mockito tests
    }

    private Person generatePerson() {
        final Person person = new Person();
        person.setFirstName(faker.name().firstName());
        person.setLastName(faker.name().lastName());
        person.setAddressLine1(faker.address().streetAddress());
        person.setAddressLine2(faker.address().streetAddress());
        person.setCity(faker.address().city());
        person.setState(State.deriveState(faker.address().state()));
        person.setExpiration(Date.from(Instant.now().plus(365, ChronoUnit.DAYS)));
        person.setAdditionalInfo(faker.letterify("??????"));
        person.setAircraftBuilt(faker.letterify("??????"));
        person.setAircraftOwned(faker.letterify("??????"));
        person.setBackgroundCheck(faker.letterify("??????"));
        person.setCellPhone(faker.phoneNumber().cellPhone());
        person.setHomePhone(faker.phoneNumber().phoneNumber());
        person.setCountry(Country.USA);
        person.setEaaNumber(faker.numerify("######"));
        person.setEaglePilot(Boolean.TRUE);
        person.setYePilot(Boolean.TRUE);
        person.setEmail(faker.internet().emailAddress());
        person.setZipCode(faker.address().zipCode());
        person.setNickname(faker.name().firstName());
        person.setSpouse(faker.name().firstName());
        person.setGender(Gender.MALE);
        return person;
    }

    private Person generateMinimalPerson() {
        final Person person = new Person();
        person.setFirstName(faker.name().firstName());
        person.setLastName(faker.name().lastName());
        person.setJoined(MDY_SDF.format(java.util.Date.from(Instant.now())));
        person.setExpiration(java.util.Date.from(Instant.now().plus(365, ChronoUnit.DAYS)));
        person.setMemberType(MemberType.Regular);
        person.setStatus(Status.ACTIVE);
        person.setWebAdminAccess(WebAdminAccess.NO_ACCESS);
        return person;
    }
}