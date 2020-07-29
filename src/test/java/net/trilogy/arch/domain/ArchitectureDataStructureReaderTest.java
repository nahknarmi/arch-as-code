package net.trilogy.arch.domain;


import net.trilogy.arch.adapter.architectureYaml.ArchitectureDataStructureReader;
import net.trilogy.arch.domain.c4.*;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.Test;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

import static net.trilogy.arch.TestHelper.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ArchitectureDataStructureReaderTest {
    @Test
    public void shouldReadMetaData() throws Exception {
        var file = new File(getClass().getResource(MANIFEST_PATH_TO_TEST_METADATA).getPath());
        var data = new ArchitectureDataStructureReader(new FilesFacade()).load(file);

        assertThat(data.getName(), is(equalTo("TestSpaces")));
        assertThat(data.getDescription(), is(equalTo("TestSpaces is a tool!")));
    }

    @Test
    public void shouldReadDecisions() throws Exception {
        var file = new File(getClass().getResource(MANIFEST_PATH_TO_TEST_DECISIONS).getPath());
        var data = new ArchitectureDataStructureReader(new FilesFacade()).load(file);

        assertThat(data.getDecisions().size(), is(equalTo(2)));

        var firstDecision = data.getDecisions().get(0);
        assertThat(firstDecision.getTitle(), is(equalTo("Docker as the containerization technology platform")));
        assertThat(decisionDate(firstDecision), is(equalTo(LocalDate.of(2018, 11, 29))));

        var secondDecision = data.getDecisions().get(1);
        assertThat(secondDecision.getTitle(), is(equalTo("Kubernetes as the container management platform")));
        assertThat(decisionDate(secondDecision), is(equalTo(LocalDate.of(2019, 11, 19))));
    }

    @Test
    public void shouldReadPeople() throws Exception {
        var file = new File(getClass().getResource(MANIFEST_PATH_TO_TEST_MODEL_PEOPLE).getPath());
        var data = new ArchitectureDataStructureReader(new FilesFacade()).load(file);

        assertThat(data.getModel().getPeople().size(), equalTo(4));

        var actualPerson = data.getModel().findPersonByName("PCA");
        var expectedPerson = C4Person.builder()
                .description("Product Chief Architect")
                .path(C4Path.path("@PCA"))
                .id("3")
                .location(C4Location.UNSPECIFIED)
                .name("PCA")
                .tags(Set.of(
                        new C4Tag("Person"),
                        new C4Tag("Trilogy System View"),
                        new C4Tag("Element")
                ))
                .relationships(List.of(
                        new C4Relationship("28", null, C4Action.USES, null, "7", "inter-team collaboration", null),
                        new C4Relationship("27", null, C4Action.USES, null, "5", "to communicate with team", null),
                        new C4Relationship("26", null, C4Action.USES, null, "8", "as a version control system", null)
                ))
                .build();
        assertThat(actualPerson, is(equalTo(expectedPerson)));
    }

    @Test
    public void shouldReadSystems() throws Exception {
        var file = new File(getClass().getResource(MANIFEST_PATH_TO_TEST_MODEL_SYSTEMS).getPath());
        var data = new ArchitectureDataStructureReader(new FilesFacade()).load(file);

        assertThat(data.getModel().getSystems().size(), is(equalTo(5)));

        var actual = (C4SoftwareSystem) data.getModel().findEntityById("6").orElseThrow(() -> new IllegalStateException("Could not find entity with id: " + "6"));
        var expected = C4SoftwareSystem.builder()
                .id("6")
                .name("SalesForce")
                .description("Book keeping")
                .location(C4Location.EXTERNAL)
                .relationships(List.of(
                        new C4Relationship("29", null, C4Action.USES, null, "11", "queries usage details to estimate monthly costs", "HTTPS")
                ))
                .tags(Set.of(
                        new C4Tag("Element"),
                        new C4Tag("Software System")
                ))
                .path(C4Path.path("c4://SalesForce"))
                .build();

        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void shouldReadContainers() throws Exception {
        var file = new File(getClass().getResource(MANIFEST_PATH_TO_TEST_MODEL_CONTAINERS).getPath());
        var data = new ArchitectureDataStructureReader(new FilesFacade()).load(file);

        assertThat(data.getModel().getContainers().size(), is(equalTo(4)));

        var actual = (C4Container) data.getModel().findEntityById("11").orElseThrow(() -> new IllegalStateException("Could not find entity with id: " + "11"));
        var expected = C4Container.builder()
                .name("DevSpaces/DevSpaces Backend")
                .id("11")
                .systemId("9")
                .description("Restful API providing capabilities for interacting with a DevSpace")
                .technology("Spring Boot")
                .path(C4Path.path("c4://DevSpaces/DevSpaces-DevSpaces Backend"))
                .tags(Set.of(
                        new C4Tag("DevSpaces Container View"),
                        new C4Tag("Element"),
                        new C4Tag("Container")
                ))
                .relationships(List.of(
                        new C4Relationship("32", null, C4Action.USES, null, "13", "to manipulate dev spaces", "HTTPS")
                ))
                .build();

        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void shouldReadComponents() throws Exception {
        var file = new File(getClass().getResource(MANIFEST_PATH_TO_TEST_MODEL_COMPONENTS).getPath());
        var data = new ArchitectureDataStructureReader(new FilesFacade()).load(file);

        assertThat(data.getModel().getComponents().size(), is(equalTo(5)));

        var actual = (C4Component) data.getModel().findEntityById("38").orElseThrow(() -> new IllegalStateException("Could not find entity with id: " + "38"));
        var expected = C4Component.builder()
                .path(C4Path.path("c4://DevSpaces/DevSpaces-DevSpaces API/DevSpaces-DevSpaces API-Sign In Controller"))
                .id("38")
                .name("DevSpaces/DevSpaces API/Sign In Controller")
                .containerId("13")
                .description("Allows users to sign in")
                .technology("Spring MVC Rest Controller")
                .url("https://devspaces.io/sign-in")
                .tags(Set.of(
                        new C4Tag("DevSpaces API Component View"),
                        new C4Tag("Component"),
                        new C4Tag("Element")
                ))
                .relationships(List.of(
                        new C4Relationship("34", null, C4Action.USES, null, "14", "Authorizes user", null)
                ))
                .srcMappings(List.of(
                        "src/bin/bash",
                        "src/bin/zsh"
                ))
                .build();

        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void shouldReadDeploymentNodes() throws Exception {
        var file = new File(getClass().getResource(MANIFEST_PATH_TO_TEST_MODEL_DEPLOYMENT_NODES).getPath());
        var data = new ArchitectureDataStructureReader(new FilesFacade()).load(file);

        assertThat(data.getModel().getDeploymentNodes().size(), is(equalTo(1)));
        assertThat(data.getModel().getDeploymentNodesRecursively().size(), is(equalTo(6)));

        var actual = (C4DeploymentNode) data.getModel().findEntityById("51").orElseThrow(() -> new IllegalStateException("Could not find entity with id: " + "51"));

        var expected = C4DeploymentNode.builder()
                .alias(null)
                .name("Docker Container - Database Server")
                .children(
                        Set.of(
                                C4DeploymentNode.builder()
                                        .id("52")
                                        .name("Database Server")
                                        .description("a database server")
                                        .tags(Set.of())
                                        .relationships(Set.of())
                                        .environment("Development")
                                        .technology("Oracle 12c")
                                        .instances(1)
                                        .containerInstances(Set.of(
                                                new C4ContainerInstance("53",
                                                        "Development",
                                                        new C4Reference("12", null),
                                                        1)
                                        ))
                                        .children(Set.of())
                                        .build()
                        ))
                .containerInstances(Set.of())
                .description("A Docker container.")
                .environment("Development")
                .id("51")
                .instances(1)
                .tags(Set.of())
                .relationships(Set.of())
                .technology("Docker")
                .build();

        assertThat(actual, is(equalTo(expected)));
    }

    private LocalDate decisionDate(ImportantTechnicalDecision decision) {
        return Instant.ofEpochMilli(decision.getDate().getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
