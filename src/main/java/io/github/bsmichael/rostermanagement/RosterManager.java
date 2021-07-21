package io.github.bsmichael.rostermanagement;

import io.github.bsmichael.rostermanagement.model.Person;
import io.github.bsmichael.rostermanagement.service.RosterManagerService;
import java.net.http.HttpClient;
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
     * HttpClient.
     */
    private final HttpClient httpClient;

    /**
     * Username to be used when interacting with eaachapters.org.
     */
    private final String username;

    /**
     * Password to be used when interacting with eaachapters.org.
     */
    private final String password;

    private final RosterManagerService service = new RosterManagerService();

    /**
     * Initializes a RosterManager instance.
     *
     * @param username Username to be used when interacting with eaachapters.org
     * @param password Password to be used when interacting with eaachapters.org
     * @param httpClient HttpClient
     */
    public RosterManager(final String username, final String password, final HttpClient httpClient) {
        this.username = username;
        this.password = password;
        this.httpClient = httpClient;
        LOGGER.info("RosterManager initialized for " + username);
    }

    /**
     * Initializes a RosterManager instance.
     *
     * @param username Username to be used when interacting with eaachapters.org
     * @param password Password to be used when interacting with eaachapters.org
     */
    public RosterManager(final String username, final String password) {
        this.username = username;
        this.password = password;
        this.httpClient = HttpClient.newBuilder().build();
        LOGGER.info("RosterManager initialized for " + username);
    }

    /**
     * Sets list of Slack user names.
     *
     * @param slackUsers list of Slack user names
     */
    public void setSlackUsers(final List<String> slackUsers) {
        service.setSlackUsers(slackUsers);
    }

    /**
     * Retrieves a list of all members.
     *
     * @return list of all members
     */
    public List<Person> getAllEntries() {
        final Map<String, String> headers = service.getHttpHeaders(httpClient, username, password);
        if (!service.isLoggedIn()) {
            service.login(httpClient, headers);
        }
        service.viewSearchMembersPage(httpClient, headers);
        if (service.isLoggedIn()) {
            service.logout(httpClient, headers);
        }
        final String data = service.fetchData(httpClient, headers);
        return service.parseRecords(data);
    }

    /**
     * Saves a member entry in the roster management system.
     *
     * @param person to be created or updated
     * @return current member information
     */
    public Person savePerson(final Person person) {
        final Map<String, String> headers = service.getHttpHeaders(httpClient, username, password);
        if (!service.isLoggedIn()) {
            service.login(httpClient, headers);
        }
        service.viewSearchMembersPage(httpClient, headers);
        if (!service.existsUser(httpClient, headers, person.getFirstName(), person.getLastName())) {
            service.addMember(httpClient, headers, person);
        } else {
            service.updateMember(httpClient, headers, person);
        }
        if (service.isLoggedIn()) {
            service.logout(httpClient, headers);
        }
        return person;
    }

    /**
     * Deletes a member from the roster management system.
     *
     * @param rosterId of the member to be deleted
     * @return success of operation
     */
    public boolean deletePerson(final Long rosterId) {
        if (rosterId == null) {
            LOGGER.info("not deleting person as no ID provided");
            return Boolean.FALSE;
        }
        LOGGER.info("deletePerson yet to be implemented");
        return Boolean.TRUE;
    }

}
