package net.trilogy.arch.adapter.google;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.docs.v1.model.Document;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.Decision;
import net.trilogy.arch.domain.architectureUpdate.Tdd;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static net.trilogy.arch.TestHelper.ROOT_PATH_TO_GOOGLE_DOC_P1S;
import static net.trilogy.arch.adapter.google.GoogleDocsAuthorizedApiFactory.GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnitParamsRunner.class)
public class GoogleDocumentReaderTest {

    private final GoogleDocsApiInterface mockedApiInterface = mock(GoogleDocsApiInterface.class);
    private final GoogleDocumentReader reader = new GoogleDocumentReader(mockedApiInterface);

    @Test
    public void shouldReturnEmptyAu() throws Exception {
        var apiResponse = new GoogleDocsApiInterface.Response(
                getJsonNodeFrom("{}"),
                new Document()
        );

        mockApiToReturnAGivenB(apiResponse, "url");

        assertThat(reader.load("url"), equalTo(ArchitectureUpdate.blank()));
    }

    @Parameters({
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-1.json | 4  | P1 ITD 1.1  | Chosen P1 decision",
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-1.json | 4  | P2 IFD 5    | [1.13 Is the P2 IFD identifier linked to the corresponding item in the P2 spec?1.14 Is the P2 IFD represented by at least one capability in the ACC and is the capability (specifically or as part of a set) referenced in a Business Goal Acceptance Criteria?]P2 IFD with just the decision statement.",

            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-2.json | 23 | P1 ITD 11.4 | Start Basic Processing as soon as the video is uploaded\\, and Complex Processing as soon as video Publishing is initiated",
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-2.json | 23 | P2 ITD 12   | Transcode media files using AWS Elemental MediaConvert",

            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-3.json | 11 | P2 ITD 7    | Deliver video content using Amazon CloudFront",
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-3.json | 11 | P1 ITD 7.3  | Apply on the fly CENC encryption using Lambda@Edge when delivering the video for streaming with download possibility disabled",

            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-4.json | 7  | P1 ITD 4.1  | Use existing interface to allow forward single document for a single user.",
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-4.json | 7  | P2 ITD 4    | Modify the existing ForwardDocument API call to support forwarding.",

            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-5.json | 7  | P1 ITD 1.3  | Introduce a configurable delay between user input event and issuing the data request",
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-5.json | 7  | P2 IF 1     | Use pagination in front-end app to speed-up UI and decrease memory footprint.",
    })
    @Test
    public void shouldReturnAuWithRequirements(String file, Integer totalRequirementsInFile, String requirementId, String requirement) throws Exception {
        mockApiWith(file, "url");

        ArchitectureUpdate result = reader.load("url");

        assertThat(result.getDecisions().size(), equalTo(totalRequirementsInFile));

        assertThat(
                result.getDecisions(),
                Matchers.hasEntry(new Decision.Id(requirementId), new Decision(requirement, List.of(Tdd.Id.blank())))
        );
    }

    @Test
    public void shouldReturnAuWithOrderedRequirements() throws IOException {
        mockApiWith(ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-4.json", "url");

        ArchitectureUpdate result = reader.load("url");
        List<String> expected = List.of("P2 IFD 1", "P2 IFD 2", "P2 IFD 3", "P2 ITD 4", "P1 ITD 4.1", "P2 ITD 5", "P1 ITD 5.1");

        int count = 0;
        for (Map.Entry<Decision.Id, Decision> entry : result.getDecisions().entrySet()) {
            Decision.Id k = entry.getKey();
            assertThat(expected.get(count), equalTo(k.toString()));
            count++;
        }
    }

    @Parameters({
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-1.json | Just a whole bunch of text goes here. Like a bunch of it. Maybe some symbols &@#*!)(@(#&*@.Maybe some paragraphs. Just a bunch\\, yknow?",
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-2.json | Video processing capabilities will be added to the Video upload mechanism with this milestone using AWS services for generating transcriptions\\, captions\\, thumbnails and video objects/scenes.",
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-3.json | This Milestone introduces the functionality for content streaming and download\\, integrates it with the Product and collects corresponding metrics for analytics purposes.",
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-4.json | Product Automates supply chain orchestration\\, providing a single view into everything that touches the supply chain ecosystem. In a supply chain ecosystem\\, hundreds or thousands of documents can be submitted through a typical buyer/supplier relationship.  Each of these documents is processed through a workflow that can be configured to place the document in a users individual review workqueue. Currently reviewers can accept or reject these documents. This milestone shall change the Product API and UI to allow user forward documents from his/her review workqueue to other users review workqueue.",
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-5.json | Currently Product UI load time is drastically slow (>30 secs to load) due to requesting large data sets (1M+ records) from the server. After the Milestone is implemented\\, REST API endpoints are extended to support pagination\\, while retaining backwards compatible behavior of loading the entire data set by default. UIs with performance problems (Appendix 1.3) are modified to take advantage of pagination and limit the initially loaded data set.",
    })
    @Test
    public void shouldReturnAuWithExecutiveSummary(String jsonFilename, String expected) throws Exception {
        mockApiWith(jsonFilename, "url");

        ArchitectureUpdate result = reader.load("url");

        assertThat(result.getP1().getExecutiveSummary(), equalTo(expected));
    }

    @Parameters({
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-1.json | http://fake-link-to-p2.com",
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-2.json | https://docs.google.com/document/d/15CnasdfasdfasdfLi1p8PDJA/edit#heading=h.ze6rbvelp0",
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-3.json | https://docs.google.com/document/d/15sCnB9pKdddddddrlYU8F",
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-4.json | https://docs.google.com/document/d/AAAAfdasfafdsfadfasdfs",
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-5.json | https://docs.google.com/document/d/15zy2hpZ5OI3XaWhha7ncO6sHWGbeA9WP",
    })
    @Test
    public void shouldReturnAuWithP2Link(String jsonFilename, String expected) throws Exception {
        mockApiWith(jsonFilename, "url");

        ArchitectureUpdate result = reader.load("url");

        assertThat(result.getP2().getLink(), equalTo(expected));
    }

    @Test
    public void shouldReturnAuWithP1Link() throws Exception {
        mockApiWith(ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-1.json", "url");

        ArchitectureUpdate result = reader.load("url");

        assertThat(result.getP1().getLink(), equalTo("url"));
    }

    @Parameters({
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-1.json | ABCD-1231         | http://fake-jira.com",
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-2.json | SPEC-12312        | https://jira.dev.com/browse/SPEC-12312",
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-3.json | SPEC-01127 (M1.5) | https://jira.devshop.com/browse/SPEC-01127",
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-4.json | SPEC-212312       | https://jira.fake.com/browse/SPEC-12321",
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-5.json | SPEC-18952        | https://jira.dev.com/browse/SPEC-18952",
    })
    @Test
    public void shouldReturnAuWithP1JiraTicket(String jsonFilename, String expectedTicket, String expectedLink) throws Exception {
        mockApiWith(jsonFilename, "url");

        ArchitectureUpdate result = reader.load("url");

        assertThat(result.getP1().getJira().getTicket(), equalTo(expectedTicket));
        assertThat(result.getP1().getJira().getLink(), equalTo(expectedLink));
    }

    @Parameters({
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-1.json | M1.0 First Milestone",
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-2.json | M1.2 - Video Processing (M1 - Upload\\, Download\\, and Stream Video)",
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-3.json | M1.5 - Content Delivery and Download (M2 - Upload\\, Download\\, and Stream Content)",
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-4.json | M1 - Document Forwarding",
            ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-5.json | M1 - Pagination",
    })
    @Test
    public void shouldReturnAuWithMilestone(String jsonFilename, String expected) throws Exception {
        mockApiWith(jsonFilename, "url");

        ArchitectureUpdate result = reader.load("url");

        assertThat(result.getMilestone(), equalTo(expected));
    }

    @SuppressWarnings("SameParameterValue")
    private void mockApiWith(String fileWhoseContentsWillBeReturned, String whenCalledWithUrl) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        final Path path = Paths.get(
                Objects.requireNonNull(
                        classLoader.getResource(fileWhoseContentsWillBeReturned)
                ).getPath()
        );
        final JsonNode sampleSpec = getJsonNodeFrom(Files.readString(path));

        mockApiToReturnAGivenB(
                new GoogleDocsApiInterface.Response(sampleSpec, null),
                whenCalledWithUrl
        );
    }

    // TODO [TEST UTIL]: Remove when no longer needed
    @Test
    @Ignore("This is not a test. Use this to generate new json from google docs if needed.")
    public void NotATest_UtilToFetchSampleP1Spec() throws GeneralSecurityException, IOException {
        String url1 = "https://docs.google.com/document/d/1xPIrv159vlRKklTABSxJx9Yq76MOrRfEdKLiVlXUQ68";
        String url2 = "https://docs.google.com/document/d/1Mhli4ZvCAAIwIguE7UY-DihkI1JsdxZjRG36QVen5aU/edit#";
        String url3 = "https://docs.google.com/document/d/1h-yiali65IQp6qXWb6qxkKvwvYTI9oshfOqJ3SmM4jQ/edit#";

        // More prod-like than the first two:
        String url4 = "https://docs.google.com/document/d/1_beIF2LWC3XgjDaUSrbXTuQ-3XENnjmmB8Hz3kw7hOs/edit";
        String url5 = "https://docs.google.com/document/d/1tbahd26QIINkEyOx3vvvY-ReShMdsIE34PRTA5W9Dng/edit";

        File productArchitectureDirectory = new File(".");

        var apiFactory = new GoogleDocsAuthorizedApiFactory();
        var api = apiFactory.getAuthorizedDocsApi(productArchitectureDirectory);
        var response1 = api.fetch(url1);
        var response2 = api.fetch(url2);
        var response3 = api.fetch(url3);
        var response4 = api.fetch(url4);
        var response5 = api.fetch(url5);

        Path tempFile1 = Files.createTempFile("arch-as-code_test-json-file", ".json");
        Path tempFile2 = Files.createTempFile("arch-as-code_test-json-file", ".json");
        Path tempFile3 = Files.createTempFile("arch-as-code_test-json-file", ".json");
        Path tempFile4 = Files.createTempFile("arch-as-code_test-json-file", ".json");
        Path tempFile5 = Files.createTempFile("arch-as-code_test-json-file", ".json");

        new ObjectMapper().writeValue(tempFile1.toFile(), response1.asJson());
        new ObjectMapper().writeValue(tempFile2.toFile(), response2.asJson());
        new ObjectMapper().writeValue(tempFile3.toFile(), response3.asJson());
        new ObjectMapper().writeValue(tempFile4.toFile(), response4.asJson());
        new ObjectMapper().writeValue(tempFile5.toFile(), response5.asJson());

        String resourcePath = " src/test/resources/";
        assertThat(
                "********* STATUS *********" +
                        "\n\nReading doc: " + url1 +
                        "\nReading doc: " + url2 +
                        "\nReading doc: " + url3 +
                        "\nReading doc: " + url4 +
                        "\nReading doc: " + url5 +
                        "\n\nUsing credentials within " + productArchitectureDirectory.toPath().resolve(GOOGLE_DOCS_API_CREDENTIALS_FOLDER_PATH).toAbsolutePath() +
                        "\n\nWritten to file " + tempFile1.toAbsolutePath() +
                        "\nWritten to file " + tempFile2.toAbsolutePath() +
                        "\nWritten to file " + tempFile3.toAbsolutePath() +
                        "\nWritten to file " + tempFile4.toAbsolutePath() +
                        "\nWritten to file " + tempFile5.toAbsolutePath() +
                        "\n\nRun: \nmv " + tempFile1.toAbsolutePath() + resourcePath + ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-1.json" +
                        " && mv " + tempFile2.toAbsolutePath() + resourcePath + ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-2.json" +
                        " && mv " + tempFile3.toAbsolutePath() + resourcePath + ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-3.json" +
                        " && mv " + tempFile4.toAbsolutePath() + resourcePath + ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-4.json" +
                        " && mv " + tempFile5.toAbsolutePath() + resourcePath + ROOT_PATH_TO_GOOGLE_DOC_P1S + "/SampleP1-5.json" +
                        "\n\nNow failing test on purpose :)" +
                        "\n\n********* STATUS *********\n\n",
                true, is(false)
        );
    }

    private JsonNode getJsonNodeFrom(String content) throws JsonProcessingException {
        return new ObjectMapper().readValue(content, JsonNode.class);
    }

    private void mockApiToReturnAGivenB(GoogleDocsApiInterface.Response a, String b) throws IOException {
        when(mockedApiInterface.fetch(b)).thenReturn(a);
    }
}
