package net.trilogy.arch.annotators;

import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.YamlArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddComponentReference;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddId;
import net.trilogy.arch.domain.architectureUpdate.YamlTddContainerByComponent;
import net.trilogy.arch.domain.architectureUpdate.TddContent;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.io.File;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static net.trilogy.arch.TestHelper.MANIFEST_PATH_TO_TEST_ANNOTATOR;
import static net.trilogy.arch.Util.first;
import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper.YAML_OBJECT_MAPPER;
import static net.trilogy.arch.annotators.ArchitectureUpdateAnnotator.annotateC4Paths;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ArchitectureUpdateAnnotatorTest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();

    private static YamlArchitectureUpdate getAuWith(List<YamlTddContainerByComponent> tddContainersByComponent) {
        return YamlArchitectureUpdate.blank()
                .toBuilder()
                .tddContainersByComponent(tddContainersByComponent)
                .build();
    }

    @Test
    public void shouldCheckIfEmptyComponentsExist() throws Exception {
        final var architecture = getArchitecture();
        final var au = YamlArchitectureUpdate.blank();

        collector.checkThat(ArchitectureUpdateAnnotator.isComponentsEmpty(architecture, au), equalTo(true));
    }

    @Test
    public void shouldCheckIfNotEmptyComponentsExistWithId() throws Exception {
        final var architecture = getArchitecture();
        final var au = YamlArchitectureUpdate.blank()
                .toBuilder()
                .tddContainersByComponent(singletonList(new YamlTddContainerByComponent(
                        new TddComponentReference("13"),
                        null,
                        false,
                        emptyMap())))
                .build();

        collector.checkThat(ArchitectureUpdateAnnotator.isComponentsEmpty(architecture, au), equalTo(false));
    }

    @Test
    public void shouldCheckIfNotEmptyComponentsExistWithPath() throws Exception {
        final var architecture = getArchitecture();
        final var au = YamlArchitectureUpdate.blank()
                .toBuilder()
                .tddContainersByComponent(
                        singletonList(new YamlTddContainerByComponent(
                                null,
                                "c4://Internet Banking System/Internet Banking System\\/API Application/Internet Banking System\\/API Application\\/Sign In Controller",
                                false,
                                emptyMap())))
                .build();

        collector.checkThat(ArchitectureUpdateAnnotator.isComponentsEmpty(architecture, au), equalTo(false));
    }

    @Test
    public void shouldAnnotateComponentPathWhenIdsAreFound() throws Exception {
        // GIVEN
        final var firstComponent = new YamlTddContainerByComponent(
                new TddComponentReference("13"),
                null, false,
                Map.of(new TddId("TDD 1.0"), new YamlTdd(null, null)));
        final var secondComponent = new YamlTddContainerByComponent(
                new TddComponentReference("14"),
                null, false,
                Map.of(new TddId("TDD 2.0"), new YamlTdd(null, null)));

        final var tddContainers = List.of(
                firstComponent,
                secondComponent);
        var au = getAuWith(tddContainers);

        //When
        au = annotateC4Paths(getArchitecture(), au);

        // Then
        assertThat(first(au.getTddContainersByComponent()).getComponentPath(), equalTo("c4://Internet Banking System/Internet Banking System\\/API Application/Internet Banking System\\/API Application\\/Sign In Controller"));
        assertThat(au.getTddContainersByComponent().get(1).getComponentPath(), equalTo("c4://Internet Banking System/Internet Banking System\\/API Application/Internet Banking System\\/API Application\\/Reset Password Controller"));
    }

    @Test
    public void shouldNotSetPathWhenIdIsNotFound() throws Exception {
        // GIVEN
        final var tddContainers = singletonList(
                new YamlTddContainerByComponent(
                        new TddComponentReference("Non Existing"),
                        null, false,
                        Map.of(new TddId("TDD 1.0"), new YamlTdd(null, null))));
        var au = getAuWith(tddContainers);

        //When
        au = annotateC4Paths(getArchitecture(), au);

        // Then
        assertThat(first(au.getTddContainersByComponent()).getComponentPath(), equalTo(null));
    }

    @Test
    public void shouldAnnotateComponentIdWhenPathIsFound() throws Exception {
        // GIVEN
        final var firstComponent = new YamlTddContainerByComponent(
                null,
                "c4://Internet Banking System/Internet Banking System\\/API Application/Internet Banking System\\/API Application\\/Sign In Controller",
                false,
                Map.of(new TddId("TDD 1.0"), new YamlTdd(null, null)));
        final var secondComponent = new YamlTddContainerByComponent(
                null,
                "c4://Internet Banking System/Internet Banking System\\/API Application/Internet Banking System\\/API Application\\/Reset Password Controller",
                false,
                Map.of(new TddId("TDD 2.0"), new YamlTdd(null, null)));
        final var tddContainers = List.of(
                firstComponent,
                secondComponent);

        var au = getAuWith(tddContainers);

        //When
        au = annotateC4Paths(getArchitecture(), au);

        // Then
        assertThat(first(au.getTddContainersByComponent()).getComponentId().getId(), equalTo("13"));
        assertThat(au.getTddContainersByComponent().get(1).getComponentId().getId(), equalTo("14"));
    }

    @Test
    public void shouldNotSetComponentIdWhenPathIsNotFound() throws Exception {
        // GIVEN
        final var tddContainers = singletonList(
                new YamlTddContainerByComponent(
                        null,
                        "Non existing path",
                        false,
                        Map.of(new TddId("TDD 1.0"), new YamlTdd(null, null))));
        var au = getAuWith(tddContainers);

        //When
        au = annotateC4Paths(getArchitecture(), au);

        // Then
        assertThat(first(au.getTddContainersByComponent()).getComponentId(), equalTo(null));
    }

    @Test
    public void shouldAnnotateAuWithTddContentFile() {
        // GIVEN
        final TddContent content1_0 = new TddContent("content", "TDD 1.0 : Component-100.txt");
        final TddContent content2_0 = new TddContent("content", "TDD 2.0 : Component-200.txt");

        final var tddContainers = List.of(
                new YamlTddContainerByComponent(
                        new TddComponentReference("100"),
                        null, false,
                        Map.of(new TddId("TDD 1.0"), new YamlTdd(null, null).withContent(content1_0))),
                new YamlTddContainerByComponent(
                        new TddComponentReference("200"),
                        null, false,
                        Map.of(new TddId("TDD 2.0"), new YamlTdd(null, null).withContent(content2_0))));
        final var au = getAuWith(tddContainers);

        // WHEN
        final var annotatedAu = ArchitectureUpdateAnnotator.annotateTddContentFiles(au);

        // THEN
        final var expectedAu = au.toBuilder()
                .tddContainersByComponent(List.of(
                        new YamlTddContainerByComponent(
                                new TddComponentReference("100"),
                                null, false,
                                Map.of(new TddId("TDD 1.0"), new YamlTdd(null, "TDD 1.0 : Component-100.txt").withContent(content1_0))),
                        new YamlTddContainerByComponent(
                                new TddComponentReference("200"),
                                null, false,
                                Map.of(new TddId("TDD 2.0"), new YamlTdd(null, "TDD 2.0 : Component-200.txt").withContent(content2_0)))))
                .build();

        collector.checkThat(annotatedAu, equalTo(expectedAu));
    }

    @Test
    public void shouldDoNothingWhenTddsHaveNoContent() {
        // GIVEN

        final var tddContainers = singletonList(new YamlTddContainerByComponent(
                new TddComponentReference("13"),
                null, false,
                Map.of(
                        new TddId("TDD 1.0"), new YamlTdd(null, null),
                        new TddId("TDD 1.1"), new YamlTdd("text", null),
                        new TddId("MatchedTDD 1.0"), new YamlTdd(null, "MatchedTDD 1.0 : Component-13.txt"))));
        final var au = getAuWith(tddContainers);

        // WHEN
        final var annotatedAu = ArchitectureUpdateAnnotator.annotateTddContentFiles(au);

        // THEN
        final var expectedAu = au.toBuilder()
                .tddContainersByComponent(
                        singletonList(new YamlTddContainerByComponent(
                                new TddComponentReference("13"),
                                null, false,
                                Map.of(new TddId("TDD 1.0"), new YamlTdd(null, null),
                                        new TddId("TDD 1.1"), new YamlTdd("text", null),
                                        new TddId("MatchedTDD 1.0"), new YamlTdd(null, "MatchedTDD 1.0 : Component-13.txt")))))
                .build();

        collector.checkThat(annotatedAu, equalTo(expectedAu));
    }

    private ArchitectureDataStructure getArchitecture() throws Exception {
        final var manifestFile = new File(getClass().getResource(MANIFEST_PATH_TO_TEST_ANNOTATOR).getPath());

        return YAML_OBJECT_MAPPER.readValue(
                new FilesFacade().readString(manifestFile.toPath()), ArchitectureDataStructure.class);
    }
}
