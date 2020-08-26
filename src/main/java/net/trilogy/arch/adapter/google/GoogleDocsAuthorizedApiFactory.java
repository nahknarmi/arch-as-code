package net.trilogy.arch.adapter.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.common.annotations.VisibleForTesting;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class GoogleDocsAuthorizedApiFactory {

    public static final String GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH = ".arch-as-code/google/";
    public static final String GOOGLE_DOCS_API_USER_CREDENTIALS_FILE_NAME = "userCredentialsDatastore";
    public static final String GOOGLE_DOCS_API_CLIENT_CREDENTIALS_FILE_NAME = "client_secret.json";
    public static final List<String> ACCESS_SCOPES = List.of(DocsScopes.DOCUMENTS_READONLY);

    private final String clientCredentialsFileName;
    private final String credentialsDirectory;
    private final NetHttpTransport httpTransport;
    private final JacksonFactory jsonFactory;
    private final CodeFlowBuilderFactory codeFlowBuilderFactory;
    private final AuthorizationCodeInstalledAppFactory authorizationCodeInstalledAppFactory;
    private final DocsFactory docsFactory;

    public GoogleDocsAuthorizedApiFactory() {
        this.clientCredentialsFileName = GOOGLE_DOCS_API_CLIENT_CREDENTIALS_FILE_NAME;
        this.credentialsDirectory = GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH;
        try {
            this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.jsonFactory = JacksonFactory.getDefaultInstance();
        this.docsFactory = new DocsFactory();
        this.codeFlowBuilderFactory = new CodeFlowBuilderFactory();
        this.authorizationCodeInstalledAppFactory = new AuthorizationCodeInstalledAppFactory();
    }

    @VisibleForTesting
    public GoogleDocsAuthorizedApiFactory(
            NetHttpTransport httpTransport,
            JacksonFactory jsonFactory,
            CodeFlowBuilderFactory codeFlowBuilderFactory,
            AuthorizationCodeInstalledAppFactory authorizationCodeInstalledAppFactory,
            DocsFactory docsFactory
    ) {
        this.clientCredentialsFileName = GOOGLE_DOCS_API_CLIENT_CREDENTIALS_FILE_NAME;
        this.credentialsDirectory = GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH;
        this.httpTransport = httpTransport;
        this.jsonFactory = jsonFactory;
        this.codeFlowBuilderFactory = codeFlowBuilderFactory;
        this.authorizationCodeInstalledAppFactory = authorizationCodeInstalledAppFactory;
        this.docsFactory = docsFactory;
    }

    public GoogleDocsApiInterface getAuthorizedDocsApi(File productArchitectureDirectory) throws IOException {
        Docs rawApi = docsFactory.make(httpTransport, jsonFactory, authorize(productArchitectureDirectory));
        return new GoogleDocsApiInterface(rawApi);
    }

    private Credential authorize(File productArchitectureDirectory) throws IOException {
        var credentialsDirectory = productArchitectureDirectory.toPath().resolve(this.credentialsDirectory).toFile();

        GoogleClientSecrets clientCredentials = loadClientCredentials(credentialsDirectory);
        DataStore<StoredCredential> userCredentials = new FileDataStoreFactory(credentialsDirectory).getDataStore(GOOGLE_DOCS_API_USER_CREDENTIALS_FILE_NAME);

        GoogleAuthorizationCodeFlow codeFlow = buildGoogleCodeFlow(userCredentials, clientCredentials);

        return executeCodeFlow(codeFlow);
    }

    private Credential executeCodeFlow(GoogleAuthorizationCodeFlow codeFlow) throws IOException {
        var authorizationCodeInstalledApp = authorizationCodeInstalledAppFactory.make(codeFlow, new LocalServerReceiver());
        return authorizationCodeInstalledApp.authorize(GOOGLE_DOCS_API_USER_CREDENTIALS_FILE_NAME);
    }

    private GoogleAuthorizationCodeFlow buildGoogleCodeFlow(DataStore<StoredCredential> userCredentials, GoogleClientSecrets clientCredentials) {
        return codeFlowBuilderFactory.make(
                httpTransport,
                jsonFactory,
                clientCredentials.getDetails().getClientId(),
                clientCredentials.getDetails().getClientSecret(),
                ACCESS_SCOPES
        ).setCredentialDataStore(
                userCredentials
        ).build();
    }

    private GoogleClientSecrets loadClientCredentials(File credentialsDirectory) throws IOException {
        var clientCredentialsFile = credentialsDirectory.toPath().resolve(this.clientCredentialsFileName).toFile();
        return GoogleClientSecrets.load(jsonFactory, new FileReader(clientCredentialsFile));
    }

    @VisibleForTesting
    static class DocsFactory {
        public Docs make(NetHttpTransport httpTransport, JacksonFactory jsonFactory, Credential credential) {
            return new Docs(
                    httpTransport,
                    jsonFactory,
                    credential
            );
        }
    }

    @VisibleForTesting
    static class AuthorizationCodeInstalledAppFactory {
        public AuthorizationCodeInstalledApp make(GoogleAuthorizationCodeFlow codeFlow, VerificationCodeReceiver receiver) {
            return new AuthorizationCodeInstalledApp(codeFlow, receiver);
        }
    }

    @VisibleForTesting
    static class CodeFlowBuilderFactory {
        public GoogleAuthorizationCodeFlow.Builder make(
                NetHttpTransport httpTransport,
                JacksonFactory jsonFactory,
                String clientId,
                String clientSecret,
                List<String> scopes
        ) {
            return new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport,
                    jsonFactory,
                    clientId,
                    clientSecret,
                    scopes
            );
        }
    }
}
