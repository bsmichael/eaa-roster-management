package com.starfireaviation.rostermanagement;

import com.starfireaviation.rostermanagement.model.Person;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.function.BiPredicate;

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
        headers.put("set-cookie", Arrays.asList("abc; 123"));
        HttpHeaders responseHeaders = HttpHeaders.of(headers, (x, y) -> x == x);
        Mockito
                .when(httpClient.send(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(httpResponse);
        Mockito.when(httpResponse.headers()).thenReturn(responseHeaders);
        Mockito.doReturn("<html></html>").when(httpResponse).body();

        rosterManager = new RosterManager(USERNAME, PASSWORD);
        rosterManager.setHttpClient(httpClient);
    }

    /**
     * Test setting of HttpClient.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setHttpClientTest() throws Exception {
        rosterManager.setHttpClient(httpClient);

        Mockito.verifyNoInteractions(httpClient);
    }

    /**
     * Test setting of Slack users.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setSlackUsersTest() throws Exception {
        rosterManager.setSlackUsers(new ArrayList<>());

        Assert.assertNotNull(rosterManager.getSlackUsers());
        Assert.assertTrue(CollectionUtils.isEmpty(rosterManager.getSlackUsers()));
    }

    /**
     * Test setting of Slack users.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void setSlackUsersNullTest() throws Exception {
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
    public void createEntryTest() throws Exception {
        final Person person = new Person();
        final Person createdPerson = rosterManager.createEntry(person);

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
    public void createEntryPersonExistsTest() throws Exception {
        Mockito.doReturn("<html>lnkViewUpdateMember</html>").when(httpResponse).body();

        final Person person = new Person();
        final Person createdPerson = rosterManager.createEntry(person);

        Assert.assertNotNull(createdPerson);
        Mockito.verify(httpClient, Mockito.times(4)).send(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(httpClient);
    }

    /**
     * Test updating of a person in the roster management system.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void updateEntryTest() throws Exception {
        final Person person = new Person();
        final Person updatedPerson = rosterManager.updateEntry(person);

        Assert.assertNotNull(updatedPerson);
        Mockito.verify(httpClient, Mockito.times(4)).send(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(httpClient);
    }

    /**
     * Test updating of a person in the roster management system.
     *
     * @throws Exception when a test error occurs
     */
    @Test
    public void updateEntryPersonExistsTest() throws Exception {
        Mockito.doReturn("<html>lnkViewUpdateMember</html>").when(httpResponse).body();

        final Person person = new Person();
        final Person updatedPerson = rosterManager.updateEntry(person);

        Assert.assertNotNull(updatedPerson);
        Mockito.verify(httpClient, Mockito.times(4)).send(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verifyNoMoreInteractions(httpClient);
    }

}