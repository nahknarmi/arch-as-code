package net.trilogy.arch.commands.architectureUpdate;

public interface AuInitializeConstants {
    String INITIAL_GOOGLE_API_AUTH_URI = "https://accounts.google.com/o/oauth2/auth";
    String INITIAL_GOOGLE_API_TOKEN_URI = "https://oauth2.googleapis.com/token";
    String INITIAL_GOOGLE_API_AUTH_PROVIDER_CERT_URL = "https://www.googleapis.com/oauth2/v1/certs";
    String INITIAL_GOOGLE_API_REDIRECT_URN = "urn:ietf:wg:oauth:2.0:oob";
    String INITIAL_GOOGLE_API_REDIRECT_URI = "http://localhost";
    String INITIAL_JIRA_BASE_URI = "http://jira.devfactory.com";
    String INITIAL_JIRA_LINK_PREFIX = "/browse/";
    String INITIAL_JIRA_GET_STORY_ENDPOINT = "/rest/api/2/issue/";
    String INITIAL_JIRA_BULK_CREATE_ENDPOINT = "/rest/api/2/issue/bulk";
}
