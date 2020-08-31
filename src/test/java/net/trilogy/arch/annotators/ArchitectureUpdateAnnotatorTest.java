package net.trilogy.arch.annotators;

import net.trilogy.arch.TestHelper;
import net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.Tdd;
import net.trilogy.arch.domain.architectureUpdate.TddContainerByComponent;
import net.trilogy.arch.domain.architectureUpdate.TddContent;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
                        List.of(new TddContainerByComponent(new Tdd.ComponentReference("13"), null,false, Map.of()))
                )
                .build();

        collector.checkThat(annotator.isComponentsEmpty(architecture, au), equalTo(false));
    }

    @Test
    public void shouldAnnotateComponentPathWhenIdsAreFound() throws Exception {
        // GIVEN
        TddContainerByComponent firstComponent = new TddContainerByComponent(
                new Tdd.ComponentReference("13"),
                null, false,
                Map.of(new Tdd.Id("TDD 1.0"), new Tdd(null, null))
        );
        TddContainerByComponent secondComponent = new TddContainerByComponent(
                new Tdd.ComponentReference("14"),
                null, false,
                Map.of(new Tdd.Id("TDD 2.0"), new Tdd(null, null))
        );

        List<TddContainerByComponent> tddContainers = List.of(
                firstComponent,
                secondComponent
        );
        ArchitectureUpdate au = getAuWith(List.of(), tddContainers);

        //When
        au = annotator.annotateC4Paths(getArchitecture(), au);

        // Then
        assertThat(au.getTddContainersByComponent().get(0).getComponentPath(), equalTo("c4://Internet Banking System/Internet Banking System\\/API Application/Internet Banking System\\/API Application\\/Sign In Controller"));
        assertThat(au.getTddContainersByComponent().get(1).getComponentPath(), equalTo("c4://Internet Banking System/Internet Banking System\\/API Application/Internet Banking System\\/API Application\\/Reset Password Controller"));
    }

    @Test
    public void shouldNotSetPathWhenIdIsNotFound() throws Exception {
        // GIVEN
        List<TddContainerByComponent> tddContainers = List.of(
                new TddContainerByComponent(
                        new Tdd.ComponentReference("Non Existing"),
                        null, false,
                        Map.of(new Tdd.Id("TDD 1.0"), new Tdd(null, null))
                )
        );
        ArchitectureUpdate au = getAuWith(List.of(), tddContainers);

        //When
        au = annotator.annotateC4Paths(getArchitecture(), au);

        // Then
        assertThat(au.getTddContainersByComponent().get(0).getComponentPath(), equalTo(null));
    }

    @Test
    public void shouldAnnotateComponentIdWhenPathIsFound() throws Exception {
        // GIVEN
        TddContainerByComponent firstComponent = new TddContainerByComponent(
                null,
                "c4://Internet Banking System/Internet Banking System\\/API Application/Internet Banking System\\/API Application\\/Sign In Controller",
                false,
                Map.of(new Tdd.Id("TDD 1.0"), new Tdd(null, null))
        );
        TddContainerByComponent secondComponent = new TddContainerByComponent(
                null,
                "c4://Internet Banking System/Internet Banking System\\/API Application/Internet Banking System\\/API Application\\/Reset Password Controller",
                false,
                Map.of(new Tdd.Id("TDD 2.0"), new Tdd(null, null))
        );
        List<TddContainerByComponent> tddContainers = List.of(
                firstComponent,
                secondComponent
        );

        ArchitectureUpdate au = getAuWith(List.of(), tddContainers);

        //When
        au = annotator.annotateC4Paths(getArchitecture(), au);

        // Then
        assertThat(au.getTddContainersByComponent().get(0).getComponentId().getId(), equalTo("13"));
        assertThat(au.getTddContainersByComponent().get(1).getComponentId().getId(), equalTo("14"));
    }

    @Test
    public void shouldNotSetComponentIdWhenPathIsNotFound() throws Exception {
        // GIVEN
        List<TddContainerByComponent> tddContainers = List.of(
                new TddContainerByComponent(
                        null,
                        "Non existing path",
                        false,
                        Map.of(new Tdd.Id("TDD 1.0"), new Tdd(null, null))
                )
        );
        ArchitectureUpdate au = getAuWith(List.of(), tddContainers);

        //When
        au = annotator.annotateC4Paths(getArchitecture(), au);

        // Then
        assertThat(au.getTddContainersByComponent().get(0).getComponentId(), equalTo(null));
    }

    @Test
    public void shouldAnnotateAuWithTddContentFile() {
        // GIVEN
        List<TddContent> tddContents = List.of(
                new TddContent("content", "TDD 1.0 : Component-100.txt"),
                new TddContent("content", "TDD 2.0 : Component-200.txt"),
                new TddContent("content", "Unrelated 2.0 : Component-Unrelated.txt")
        );
        List<TddContainerByComponent> tddContainers = List.of(
                new TddContainerByComponent(
                        new Tdd.ComponentReference("100"),
                        null, false,
                        Map.of(new Tdd.Id("TDD 1.0"), new Tdd(null, null))
                ),
                new TddContainerByComponent(
                        new Tdd.ComponentReference("200"),
                        null, false,
                        Map.of(new Tdd.Id("TDD 2.0"), new Tdd(null, null))
                )
        );
        ArchitectureUpdate au = getAuWith(tddContents, tddContainers);

        // WHEN
        ArchitectureUpdate annotatedAu = annotator.annotateTddContentFiles(au);

        // THEN
        ArchitectureUpdate expectedAu = au.toBuilder()
                .tddContainersByComponent(
                        List.of(
                                new TddContainerByComponent(
                                        new Tdd.ComponentReference("100"),
                                        null, false,
                                        Map.of(new Tdd.Id("TDD 1.0"), new Tdd(null, "TDD 1.0 : Component-100.txt"))
                                ),
                                new TddContainerByComponent(
                                        new Tdd.ComponentReference("200"),
                                        null, false,
                                        Map.of(new Tdd.Id("TDD 2.0"), new Tdd(null, "TDD 2.0 : Component-200.txt"))
                                )
                        )
                ).build();

        collector.checkThat(annotatedAu, equalTo(expectedAu));
    }

    @Test
    public void shouldDoNothingWhenNoMatch() {
        // GIVEN
        List<TddContent> tddContents = List.of(
                new TddContent("content", "MatchedTDD 1.0 : Component-13.txt"),
                new TddContent("content", "Unrelated 1.0 : Component-13.txt"),
                new TddContent("content", "TDD 1.0 : Component-Unrelated.txt"),
                new TddContent("content", "Unrelated 2.0 : Component-Unrelated.txt")
        );
        List<TddContainerByComponent> tddContainers = List.of(new TddContainerByComponent(
                        new Tdd.ComponentReference("13"),
                null, false,
                        Map.of(
                                new Tdd.Id("TDD 1.0"), new Tdd(null, null),
                                new Tdd.Id("TDD 1.1"), new Tdd("text", null),
                                new Tdd.Id("MatchedTDD 1.0"), new Tdd(null, "MatchedTDD 1.0 : Component-13.txt")
                        )
                )
        );
        ArchitectureUpdate au = getAuWith(tddContents, tddContainers);

        // WHEN
        ArchitectureUpdate annotatedAu = annotator.annotateTddContentFiles(au);

        // THEN
        ArchitectureUpdate expectedAu = au.toBuilder()
                .tddContainersByComponent(
                        List.of(new TddContainerByComponent(
                                new Tdd.ComponentReference("13"),
                                null, false,
                                Map.of(new Tdd.Id("TDD 1.0"), new Tdd(null, null),
                                        new Tdd.Id("TDD 1.1"), new Tdd("text", null),
                                        new Tdd.Id("MatchedTDD 1.0"), new Tdd(null, "MatchedTDD 1.0 : Component-13.txt")))
                        )
                ).build();

        collector.checkThat(annotatedAu, equalTo(expectedAu));
    }

    private ArchitectureUpdate getAuWith(List<TddContent> tddContents, List<TddContainerByComponent> tddContainersByComponent) {
        return ArchitectureUpdate.blank()
                .toBuilder()
                .tddContents(tddContents)
                .tddContainersByComponent(tddContainersByComponent)
                .build();
    }

    private ArchitectureDataStructure getArchitecture() throws Exception {
        File manifestFile = new File(getClass().getResource(TestHelper.MANIFEST_PATH_TO_TEST_ANNOTATOR).getPath());
        return new ArchitectureDataStructureObjectMapper()
                .readValue(new FilesFacade().readString(manifestFile.toPath()));
    }
}
