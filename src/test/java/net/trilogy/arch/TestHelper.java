package net.trilogy.arch;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class TestHelper {
    public static final String MANIFEST_PATH_TO_TEST_GENERALLY = "/architecture/products/testspaces/product-architecture.yml";
    public static final String MANIFEST_PATH_TO_TEST_DECISIONS = MANIFEST_PATH_TO_TEST_GENERALLY;
    public static final String MANIFEST_PATH_TO_TEST_MODEL_PEOPLE = MANIFEST_PATH_TO_TEST_GENERALLY;
    public static final String MANIFEST_PATH_TO_TEST_MODEL_SYSTEMS = MANIFEST_PATH_TO_TEST_GENERALLY;
    public static final String MANIFEST_PATH_TO_TEST_MODEL_CONTAINERS = MANIFEST_PATH_TO_TEST_GENERALLY;
    public static final String MANIFEST_PATH_TO_TEST_MODEL_COMPONENTS = MANIFEST_PATH_TO_TEST_GENERALLY;
    public static final String MANIFEST_PATH_TO_TEST_METADATA = MANIFEST_PATH_TO_TEST_GENERALLY;
    public static final String MANIFEST_PATH_TO_TEST_JIRA_STORY_CREATION = "/au/auAnnotate/product-architecture.yml";

    public static final String MANIFEST_PATH_TO_TEST_AU_VALIDATION_BEFORE_UPDATE = "/au/auValidation/unit/archBeforeAu.yml";
    public static final String MANIFEST_PATH_TO_TEST_AU_VALIDATION_AFTER_UPDATE = "/au/auValidation/unit/archAfterAu.yml";

    public static final String MANIFEST_PATH_TO_TEST_SERIALIZATION = "/yaml/big-bank.yml";
    public static final String MANIFEST_PATH_TO_TEST_MODEL_DEPLOYMENT_NODES = "/architecture/products/bigBank/product-architecture.yml";

    public static final String JSON_STRUCTURIZR_TEST_SPACES = "/structurizr/test-spaces.json";
    public static final String JSON_STRUCTURIZR_BIG_BANK = "/structurizr/big-bank.json";
    public static final String JSON_STRUCTURIZR_THINK3_SOCOCO = "/structurizr/Think3-Sococo.c4model.json";
    public static final String JSON_STRUCTURIZR_EMPTY = "/structurizr/empty-workspace.json";
    public static final String JSON_STRUCTURIZR_EMBEDDED_IMAGE = "/structurizr/embedded-image.json";
    public static final String JSON_STRUCTURIZR_NO_SYSTEM = "/structurizr/no-system-deployment-view-workspace.json";
    public static final String JSON_STRUCTURIZR_TRICKY_DEPLOYMENT_NODE_SCOPES = "/structurizr/deployment-node-scopes.json";
    public static final String JSON_STRUCTURIZR_MULTIPLE_RELATIONSHIPS = "/structurizr/multiple-relationships-with-same-source-and-destination.json";

    public static final String JSON_JIRA_GET_EPIC = "/jira/get-epic-response.json";
    public static final String JSON_JIRA_CREATE_STORIES_REQUEST_EXPECTED_BODY = "/jira/create-stories-request-expected-body.json";
    public static final String JSON_JIRA_CREATE_STORIES_RESPONSE_EXPECTED_BODY = "/jira/create-stories-response-expected-body.json";

    public static final String ROOT_PATH_TO_TEST_GENERALLY = "/architecture/products/testspaces/";
    public static final String ROOT_PATH_TO_TEST_PRODUCT_DOCUMENTATION = "/architecture/products/testspaces/";
    public static final String ROOT_PATH_TO_TEST_DIFF_COMMAND = "/architecture/products/testspaces/";
    public static final String ROOT_PATH_TO_TEST_VALIDATION = "/yaml/validation/";
    public static final String ROOT_PATH_TO_TEST_AU_VALIDATION_E2E = "/au/auValidation/e2e/";
    public static final String ROOT_PATH_TO_TEST_AU_PUBLISH = "/au/auPublish/";
    public static final String ROOT_PATH_TO_TEST_VIEWS = "/architecture/products/bigBank/";
    public static final String ROOT_PATH_TO_TEST_AU_ANNOTATE = "/au/auAnnotate/";

    public static final String ROOT_PATH_TO_GOOGLE_DOC_P1S = "google-docs/P1";

    public static final String MARKDOWN_DOCUMENTATION_FILE = "/architecture/products/testspaces/documentation/1_context-diagram.md";
    public static final String ASCII_DOCUMENTATION_FILE = "/architecture/products/testspaces/documentation/3_Ascii-docs.txt";
    public static final String NO_ORDER_DOCUMENTATION_FILE = "/architecture/products/testspaces/documentation/no_order.txt";

    public static final String IMAGE_THOUGHTWORKS_FILE = "/images/thoughtworks.png";

    public static Integer execute(String... args) {
        return Application.builder().build().execute(args);
    }

    public static Integer execute(String command) {
        return Application.builder().build().execute(command.split(" "));
    }

    public static Integer execute(Application application, String command) {
        return application.execute(command.split(" "));
    }

    public static Integer execute(Application application, String... args) {
        return application.execute(args);
    }

    public static Path getPath(Class<?> getClassOfCallingClass, String file) {
        return new File(getClassOfCallingClass.getResource(file).getPath()).toPath();
    }

    public static String loadResource(Class<?> getClassOfCallingClass, String file) throws Exception {
        return Files.readString(Path.of(getClassOfCallingClass.getResource(file).toURI()));
    }
}
