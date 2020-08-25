package net.trilogy.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.documentation.Image;
import com.structurizr.documentation.Section;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static net.trilogy.arch.TestHelper.ROOT_PATH_TO_TEST_PRODUCT_DOCUMENTATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DocumentationEnhancerTest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();

    final PrintStream originalOut = System.out;
    final PrintStream originalErr = System.err;
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @Before
    public void setUp() throws Exception {
        out.reset();
        err.reset();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @After
    public void tearDown() throws Exception {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void should_have_two_sections_corresponding_to_two_markdown_files() {
        File root = mock(File.class);
        Workspace workspace = new Workspace("Foo", "Bazz");
        ArchitectureDataStructure dataStructure = mock(ArchitectureDataStructure.class);

        when(root.getAbsolutePath()).thenReturn(getClass().getResource(ROOT_PATH_TO_TEST_PRODUCT_DOCUMENTATION).getPath());
        when(dataStructure.getName()).thenReturn("testspaces");

        new DocumentationEnhancer(root, new FilesFacade()).enhance(workspace, dataStructure);

        assertThat(workspace.getDocumentation().getSections(), hasSize(4));
    }

    @Test
    public void shouldPreserveOrderOfDocumentation() {
        // Given
        Workspace workspace = new Workspace("Foo", "Bazz");
        final String file = getClass().getResource(ROOT_PATH_TO_TEST_PRODUCT_DOCUMENTATION).getFile();

        //When
        new DocumentationEnhancer(new File(file), new FilesFacade()).enhance(workspace, new ArchitectureDataStructure());

        // Then
        collector.checkThat(workspace.getDocumentation().getSections().size(), equalTo(4));

        final var expected = Map.of(
                1, "context-diagram",
                2, "functional-overview",
                3, "Ascii-docs",
                4, "no_order"
        );
        final var actual = workspace.getDocumentation().getSections().stream()
                .collect(Collectors.toMap(Section::getOrder, Section::getTitle));

        collector.checkThat(actual, equalTo(expected));
    }

    @Test
    public void shouldLogErrorsAndContinueWhenLoadingDocumentation() throws IOException {
        // Given
        Workspace workspace = new Workspace("Foo", "Bazz");
        final FilesFacade mockedFilesFacade = mock(FilesFacade.class);
        when(mockedFilesFacade.readString(any())).thenThrow(new IOException("Boom!"));
        final String file = getClass().getResource(ROOT_PATH_TO_TEST_PRODUCT_DOCUMENTATION).getFile();

        // When
        new DocumentationEnhancer(new File(file), mockedFilesFacade).enhance(workspace, new ArchitectureDataStructure());

        // Then
        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(err.toString(), containsString("Unable to import documentation: 1_context-diagram.md\nError thrown: java.io.IOException: Boom!"));
        collector.checkThat(err.toString(), containsString("Unable to import documentation: 2_functional-overview.md\nError thrown: java.io.IOException: Boom!"));
        collector.checkThat(err.toString(), containsString("Unable to import documentation: 3_Ascii-docs.txt\nError thrown: java.io.IOException: Boom!"));
        collector.checkThat(err.toString(), containsString("Unable to import documentation: no_order.txt\nError thrown: java.io.IOException: Boom!"));
    }

    @Test
    public void shouldAddDocumentationImages() {
        // Given
        Workspace workspace = new Workspace("Foo", "Bar");
        final String file = getClass().getResource(ROOT_PATH_TO_TEST_PRODUCT_DOCUMENTATION).getFile();

        // When
        new DocumentationEnhancer(new File(file), new FilesFacade()).enhance(workspace, new ArchitectureDataStructure());

        // Then
        Set<Image> images = workspace.getDocumentation().getImages();
        collector.checkThat(images.size(), equalTo(1));
        Image image = (Image) new ArrayList(images).get(0);
        collector.checkThat(image.getName(), equalTo("devfactory.png"));
    }
}
