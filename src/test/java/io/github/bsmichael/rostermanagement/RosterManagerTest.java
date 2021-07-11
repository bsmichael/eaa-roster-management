package io.github.bsmichael.rostermanagement;

import com.github.javafaker.Faker;
import io.github.bsmichael.rostermanagement.model.Country;
import io.github.bsmichael.rostermanagement.model.Gender;
import io.github.bsmichael.rostermanagement.model.Person;
import io.github.bsmichael.rostermanagement.model.State;
import org.apache.commons.collections4.CollectionUtils;
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

    /**
     * Test setting of Slack users.
     *
     */
    @Test
    public void setSlackUsersTest() {
        rosterManager.setSlackUsers(new ArrayList<>());

        Assert.assertNotNull(rosterManager.getSlackUsers());
        Assert.assertTrue(CollectionUtils.isEmpty(rosterManager.getSlackUsers()));
    }

    /**
     * Test setting of Slack users.
     *
     */
    @Test
    public void setSlackUsersNullTest() {
        rosterManager.setSlackUsers(null);

        Assert.assertNotNull(rosterManager.getSlackUsers());
        Assert.assertTrue(CollectionUtils.isEmpty(rosterManager.getSlackUsers()));
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

        Mockito.verify(httpClient, Mockito.times(4)).send(ArgumentMatchers.any(), ArgumentMatchers.any());
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
        Mockito.verify(httpClient, Mockito.times(4)).send(ArgumentMatchers.any(), ArgumentMatchers.any());
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
        Mockito.verify(httpClient, Mockito.times(4)).send(ArgumentMatchers.any(), ArgumentMatchers.any());
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
        Mockito.verify(httpClient, Mockito.times(4)).send(ArgumentMatchers.any(), ArgumentMatchers.any());
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
}