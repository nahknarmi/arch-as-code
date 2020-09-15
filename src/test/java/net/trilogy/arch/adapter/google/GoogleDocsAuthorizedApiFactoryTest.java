package net.trilogy.arch.adapter.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.DocsScopes;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static net.trilogy.arch.adapter.google.GoogleDocsAuthorizedApiFactory.ACCESS_SCOPES;
import static net.trilogy.arch.adapter.google.GoogleDocsAuthorizedApiFactory.GOOGLE_DOCS_API_CLIENT_CREDENTIALS_FILE_NAME;
import static net.trilogy.arch.adapter.google.GoogleDocsAuthorizedApiFactory.GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH;
import static net.trilogy.arch.adapter.google.GoogleDocsAuthorizedApiFactory.GOOGLE_DOCS_API_USER_CREDENTIALS_FILE_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;

/*
 * NOTE: Mockito Strict Stubbing is required. This test walks through
 *     the process of obtaining a google API connection by
 *     mocking each individual step. Strict stubbing acts as assertions
 *     throughout this process.
 */
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class GoogleDocsAuthorizedApiFactoryTest {
    private static final String CLIENT_ID = "client-id";
    private static final String CLIENT_SECRET = "client-secret";
    @Rule
    public final ErrorCollector collector = new ErrorCollector();
    private final List<String> SCOPES = List.of(DocsScopes.DOCUMENTS_READONLY);
    private final NetHttpTransport httpTransport = new NetHttpTransport();
    private final JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    private GoogleDocsAuthorizedApiFactory.AuthorizationCodeInstalledAppFactory mockedAuthorizationCodeInstalledAppFactory;
    private GoogleDocsAuthorizedApiFactory.CodeFlowBuilderFactory mockedCodeFlowBuilderFactory;
    private GoogleDocsAuthorizedApiFactory.DocsFactory mockedDocsFactory;
    private Path rootDir;

    @Before
    public void setUp() throws Exception {
        rootDir = Files.createTempDirectory("arch-as-code_tests");

        Path userCredentialsDirectory = rootDir.resolve(GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH);

        //noinspection ResultOfMethodCallIgnored
        userCredentialsDirectory.toFile().mkdirs();

        Path clientCredentialsFile = userCredentialsDirectory.resolve(GOOGLE_DOCS_API_CLIENT_CREDENTIALS_FILE_NAME);
        Files.writeString(
                clientCredentialsFile,
                "{" +
                        "\"installed\":" +
                        "{" +
                        "\"client_id\":\"" + CLIENT_ID + "\"," +
                        "\"project_id\":\"proj-id\"," +
                        "\"auth_uri\":\"https://accounts.fake.com/o/oauth2/auth\"," +
                        "\"token_uri\":\"https://oauth2.fakeapis.com/token\"," +
                        "\"auth_provider_x509_cert_url\":\"https://www.fakeapis.com/oauth2/v1/certs\"," +
                        "\"client_secret\":\"" + CLIENT_SECRET + "\"," +
                        "\"redirect_uris\":[\"urn:ietf:wg:oauth:2.0:oob\",\"http://localhost\"]" +
                        "}" +
                        "}"
        );

        mockedAuthorizationCodeInstalledAppFactory = Mockito.mock(GoogleDocsAuthorizedApiFactory.AuthorizationCodeInstalledAppFactory.class);
        mockedCodeFlowBuilderFactory = Mockito.mock(GoogleDocsAuthorizedApiFactory.CodeFlowBuilderFactory.class);
        mockedDocsFactory = Mockito.mock(GoogleDocsAuthorizedApiFactory.DocsFactory.class);
    }

    @Test
    public void shouldUseTheRightConstants() {
        collector.checkThat(GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH, equalTo(".arch-as-code/google/"));
        collector.checkThat(GOOGLE_DOCS_API_USER_CREDENTIALS_FILE_NAME, equalTo("userCredentialsDatastore"));
        collector.checkThat(GOOGLE_DOCS_API_CLIENT_CREDENTIALS_FILE_NAME, equalTo("client_secret.json"));
        collector.checkThat(ACCESS_SCOPES, contains(DocsScopes.DOCUMENTS_READONLY));
    }

    @Test
    public void shouldCallTheGoogleApiInTheCorrectSequence() throws Exception {
        Docs docsApi = setBehaviourOnMockedGoogleApiBuilderClassesToReturnMockedApi();
        GoogleDocsApiInterface expected = new GoogleDocsApiInterface(docsApi);

        var result = new GoogleDocsAuthorizedApiFactory(
                httpTransport,
                jsonFactory,
                mockedCodeFlowBuilderFactory,
                mockedAuthorizationCodeInstalledAppFactory,
                mockedDocsFactory
        ).getAuthorizedDocsApi(
                rootDir.toFile()
        );

        assertThat(result, equalTo(expected));
    }

    private Docs setBehaviourOnMockedGoogleApiBuilderClassesToReturnMockedApi() throws IOException {
        GoogleAuthorizationCodeFlow googleCodeFlowResult = mockGoogleCodeFlow();
        Credential googleAuthorizeFlowResult = mockGoogleAuthorizeFlow(googleCodeFlowResult);

        //noinspection UnnecessaryLocalVariable
        Docs googleDocsFlowResult = mockGoogleDocsFlow(googleAuthorizeFlowResult);

        return googleDocsFlowResult;
    }

    private Docs mockGoogleDocsFlow(Credential googleAuthorizeFlowResult) {
        var mockedDocsApi = Mockito.mock(Docs.class);
        Mockito.when(mockedDocsFactory.make(httpTransport, jsonFactory, googleAuthorizeFlowResult)).thenReturn(mockedDocsApi);
        return mockedDocsApi;
    }

    private Credential mockGoogleAuthorizeFlow(GoogleAuthorizationCodeFlow googleCodeFlowResult) throws IOException {
        var mockedAuthorization = Mockito.mock(AuthorizationCodeInstalledApp.class);
        Mockito.when(
                mockedAuthorizationCodeInstalledAppFactory.make(same(googleCodeFlowResult), any())
        ).thenReturn(
                mockedAuthorization
        );

        var mockedCredential = Mockito.mock(Credential.class);
        Mockito.when(mockedAuthorization.authorize(GOOGLE_DOCS_API_USER_CREDENTIALS_FILE_NAME)).thenReturn(mockedCredential);
        return mockedCredential;
    }

    private GoogleAuthorizationCodeFlow mockGoogleCodeFlow() {
        var mockedCodeFlowBuilder = Mockito.mock(GoogleAuthorizationCodeFlow.Builder.class);

        // TODO [TESTING] [MINOR]: Replace any() to assert that the right credential data store is being used.
        Mockito.when(mockedCodeFlowBuilder.setCredentialDataStore(any())).thenReturn(mockedCodeFlowBuilder);

        Mockito.when(
                mockedCodeFlowBuilderFactory.make(httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, SCOPES)
        ).thenReturn(
                mockedCodeFlowBuilder
        );

        var mockedCodeFlow = Mockito.mock(GoogleAuthorizationCodeFlow.class);
        Mockito.when(mockedCodeFlowBuilder.build()).thenReturn(mockedCodeFlow);
        return mockedCodeFlow;
    }
}
