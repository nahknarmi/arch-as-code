package net.trilogy.arch.annotators;

import net.trilogy.arch.TestHelper;
import net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.Tdd;
import net.trilogy.arch.domain.architectureUpdate.TddContainerByComponent;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ArchitectureUpdateAnnotatorTest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();


    private ArchitectureUpdateAnnotator annotator;

    @Before
    public void setUp() {
        annotator = new ArchitectureUpdateAnnotator();
    }

    @Test
    public void shouldCheckIfEmptyComponentsExist() throws Exception {
        ArchitectureDataStructure architecture = getArchitecture();
        ArchitectureUpdate au = ArchitectureUpdate.blank();

        collector.checkThat(annotator.isComponentsEmpty(architecture, au), equalTo(true));
    }

    @Test
    public void shouldCheckIfNotEmptyComponentsExist() throws Exception {
        ArchitectureDataStructure architecture = getArchitecture();
        ArchitectureUpdate au = ArchitectureUpdate.blank()
                .toBuilder()
                .tddContainersByComponent(
                        List.of(new TddContainerByComponent(new Tdd.ComponentReference("13"), false, Map.of()))
                )
                .build();

        collector.checkThat(annotator.isComponentsEmpty(architecture, au), equalTo(false));
    }

    @Test
    public void shouldAnnotateIds() throws Exception {
        String auLineSingleQuote = "\n- component-id: '13'\n";
        String auLineDoubleQuote = "\n- component-id: \"13\"\n";

        String expectedSingleQuote = "\n- component-id: '13'  # c4://Internet Banking System/Internet Banking System/API Application/Internet Banking System/API Application/Sign In Controller\n";
        String expectedDoubleQuote = "\n- component-id: \"13\"  # c4://Internet Banking System/Internet Banking System/API Application/Internet Banking System/API Application/Sign In Controller\n";

        collector.checkThat(annotator.annotateC4Paths(getArchitecture(), auLineSingleQuote), equalTo(expectedSingleQuote));
        collector.checkThat(annotator.annotateC4Paths(getArchitecture(), auLineDoubleQuote), equalTo(expectedDoubleQuote));
    }

    @Test
    public void shouldAnnotateIndentedIds() throws Exception {
        String auLineSingleQuote = "\n  - component-id: '13'\n";
        String auLineDoubleQuote = "\n  - component-id: \"13\"\n";

        String expectedSingleQuote = "\n  - component-id: '13'  # c4://Internet Banking System/Internet Banking System/API Application/Internet Banking System/API Application/Sign In Controller\n";
        String expectedDoubleQuote = "\n  - component-id: \"13\"  # c4://Internet Banking System/Internet Banking System/API Application/Internet Banking System/API Application/Sign In Controller\n";

        collector.checkThat(annotator.annotateC4Paths(getArchitecture(), auLineSingleQuote), equalTo(expectedSingleQuote));
        collector.checkThat(annotator.annotateC4Paths(getArchitecture(), auLineDoubleQuote), equalTo(expectedDoubleQuote));
    }

    @Test
    public void shouldAnnotateNonNumberStringIds() throws Exception {
        annotator = spy(ArchitectureUpdateAnnotator.class);
        when(annotator.getComponentPathComment(any(ArchitectureDataStructure.class), eq("ArbitraryId42")))
                .thenReturn("  # c4://CommentPath");

        String auLine = "\n- component-id: \"ArbitraryId42\"\n";
        String expected = "\n- component-id: \"ArbitraryId42\"  # c4://CommentPath\n";

        collector.checkThat(this.annotator.annotateC4Paths(getArchitecture(), auLine), equalTo(expected));
    }


    @Test
    public void shouldLeaveLineUnchangedIfIdNotFound() throws Exception {
        String auLine = "\n- component-id: \"DoesNotExist\"\n";
        String expected = "\n- component-id: \"DoesNotExist\"\n";

        collector.checkThat(annotator.annotateC4Paths(getArchitecture(), auLine), equalTo(expected));
    }

    @Test
    public void shouldReturnPathComment() throws Exception {
        String id = "13";
        String expectedComment = "  # c4://Internet Banking System/Internet Banking System\\/API Application/Internet Banking System\\/API Application\\/Sign In Controller";
        collector.checkThat(annotator.getComponentPathComment(getArchitecture(), id), equalTo(expectedComment));
    }

    @Test
    public void shouldReturnEmptyComment() throws Exception {
        String id = "DoesNotExist";
        String expectedComment = "";
        collector.checkThat(annotator.getComponentPathComment(getArchitecture(), id), equalTo(expectedComment));
    }

    private ArchitectureDataStructure getArchitecture() throws Exception {
        File manifestFile = new File(getClass().getResource(TestHelper.MANIFEST_PATH_TO_TEST_ANNOTATOR).getPath());
        return new ArchitectureDataStructureObjectMapper()
                .readValue(new FilesFacade().readString(manifestFile.toPath()));
    }
}
