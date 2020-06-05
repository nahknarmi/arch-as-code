package net.trilogy.arch.adapter;

import net.trilogy.arch.TestHelper;
import net.trilogy.arch.adapter.structurizr.WorkspaceReader;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.ImportantTechnicalDecision;
import net.trilogy.arch.domain.c4.*;
import net.trilogy.arch.domain.c4.view.C4DeploymentView;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static net.trilogy.arch.domain.c4.C4Action.USES;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


public class WorkspaceReaderTest {

    @Test
    public void shouldHaveValidDescription() throws Exception {
        URL resource = getClass().getResource(TestHelper.JSON_STRUCTURIZR_EMPTY);
        ArchitectureDataStructure dataStructure = new WorkspaceReader().load(new File(resource.getPath()));

        assertThat(dataStructure.getDescription(), equalTo(""));
    }

    @Test
    public void shouldReadComponent() throws Exception {
        URL resource = getClass().getResource(TestHelper.JSON_STRUCTURIZR_THINK3_SOCOCO);
        ArchitectureDataStructure data = new WorkspaceReader().load(new File(resource.getPath()));

        C4Component component = (C4Component) data.getModel().findEntityById("220").orElseThrow(() -> new IllegalStateException("Could not find entity with id: " + "220"));

        assertThat(component.getName(), is(equalTo("Ionic")));
        assertThat(component.getContainerAlias(), is(nullValue()));
        assertThat(component.getAlias(), is(nullValue()));
        assertThat(component.getTags(), containsInAnyOrder(
                new C4Tag("Element"),
                new C4Tag("Component"),
                new C4Tag("External")
        ));
        assertThat(component.getDescription(), is(equalTo("Ionic native part for Android")));
        assertThat(component.getContainerId(), is(equalTo("219")));
        assertThat(component.getType(), is(equalTo(C4Type.component)));
        assertThat(component.getUrl(), is(nullValue()));
        assertThat(component.getPath(), is(C4Path.path("c4://Sococo Virtual Office/Android App/Ionic")));
        assertThat(component.getRelationships(), contains(
                new C4Relationship("239", null, USES, null, "16", "Runs", "Chromium")
        ));
        assertThat(component.getTechnology(), is(equalTo("Android")));
        assertThat(component.getSrcMappings(), contains("src/bin/bash", "src/bin/zsh"));
    }

    @Test
    public void shouldReadCorrectNumberOfElements() throws Exception {
        URL resource = getClass().getResource(TestHelper.JSON_STRUCTURIZR_THINK3_SOCOCO);
        ArchitectureDataStructure dataStructure = new WorkspaceReader().load(new File(resource.getPath()));

        assertThat(dataStructure.getName(), is(equalTo("Sococo Import")));
        assertThat(dataStructure.getBusinessUnit(), is(equalTo("Think3")));

        assertThat(dataStructure, is(notNullValue()));
        assertThat(dataStructure.getModel(), is(notNullValue()));
        MatcherAssert.assertThat(dataStructure.getModel().getPeople(), hasSize(7));
        MatcherAssert.assertThat(dataStructure.getModel().getSystems(), hasSize(13));
        MatcherAssert.assertThat(dataStructure.getModel().getContainers(), hasSize(28));
        MatcherAssert.assertThat(dataStructure.getModel().getComponents(), hasSize(35));
        MatcherAssert.assertThat(dataStructure.getModel().allRelationships(), hasSize(160));

        MatcherAssert.assertThat(dataStructure.getViews().getComponentViews(), hasSize(8));
        MatcherAssert.assertThat(dataStructure.getViews().getContainerViews(), hasSize(9));
        MatcherAssert.assertThat(dataStructure.getViews().getSystemViews(), hasSize(6));
    }

    @Test
    public void shouldReadDeploymentNodes() throws Exception {
        URL resource = getClass().getResource(TestHelper.JSON_STRUCTURIZR_BIG_BANK);
        ArchitectureDataStructure dataStructure = new WorkspaceReader().load(new File(resource.getPath()));

        assertThat(dataStructure.getModel().getDeploymentNodes().size(), is(equalTo(4)));
        assertThat(dataStructure.getModel().getDeploymentNodesRecursively().size(), is(equalTo(18)));

        var actual = (C4DeploymentNode) dataStructure.getModel().findEntityById("65").orElseThrow(() -> new IllegalStateException("Could not find entity with id: " + "65"));

        var expected = C4DeploymentNode.builder()
                .alias(null)
                .id("65")
                .name("Customer's computer")
                .children(List.of(
                        C4DeploymentNode.builder()
                                .id("66")
                                .name("Web Browser")
                                .environment("Live")
                                .technology("Chrome, Firefox, Safari, or Edge")
                                .instances(1)
                                .containerInstances(List.of(
                                        new C4ContainerInstance("67", "Live", new C4Reference("17", null), 2)
                                ))
                                .children(List.of())
                                .build()
                ))
                .containerInstances(List.of())
                .environment("Live")
                .technology("Microsoft Windows or Apple macOS")
                .instances(1)
                .tags(Set.of())
                .relationships(List.of())
                .build();

        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void shouldReadDeploymentViews() throws Exception {
        URL resource = getClass().getResource(TestHelper.JSON_STRUCTURIZR_BIG_BANK);
        ArchitectureDataStructure dataStructure = new WorkspaceReader().load(new File(resource.getPath()));
        C4DeploymentView actual = dataStructure.getViews().getDeploymentViews().stream()
                .filter(v -> v.getKey().equals("DevelopmentDeployment")).findAny().get();

        C4DeploymentView expected = C4DeploymentView.builder()
                .key("DevelopmentDeployment")
                .name("Internet Banking System - Deployment - Development")
                .system(new C4Reference("2", null))
                .description("An example development deployment scenario for the Internet Banking System.")
                .environment("Development")
                .elements(Set.of(new C4Reference("50", null)))
                .build();

        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void shouldReadDeploymentViewWithNoSystem() throws Exception {
        URL resource = getClass().getResource(TestHelper.JSON_STRUCTURIZR_NO_SYSTEM);
        final String key = "test";

        ArchitectureDataStructure dataStructure = new WorkspaceReader().load(new File(resource.getPath()));
        C4DeploymentView actual = dataStructure.getViews().getDeploymentViews().stream()
                .filter(v -> v.getKey().equals(key)).findAny().get();

        C4DeploymentView expected = C4DeploymentView.builder()
                .key(key)
                .environment("Default")
                .description("")
                .name("Deployment - Default")
                .elements(Set.of(new C4Reference("1", null)))
                .build();

        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void shouldReadDecisions() throws Exception {
        URL resource = getClass().getResource(TestHelper.JSON_STRUCTURIZR_THINK3_SOCOCO);
        String id = "4";

        ArchitectureDataStructure dataStructure = new WorkspaceReader().load(new File(resource.getPath()));
        List<ImportantTechnicalDecision> decisions = dataStructure.getDecisions();

        assertThat(decisions.size(), is(equalTo(4)));
        ImportantTechnicalDecision actual = decisions.stream().filter(d -> d.getId().equals(id)).findFirst().get();

        ImportantTechnicalDecision expected = ImportantTechnicalDecision.builder()
                .id(id)
                .title("Deploy Single Page Application container build onto CDN")
                .date(Date.from(LocalDateTime.parse("2020-01-12T09:36:04Z", ISO_DATE_TIME).toInstant(UTC)))
                .content("## Context\nSingle Page Application is written in Angular and supports webpack builds.\n\n## Decision\nTBD\n\n## Consequences")
                .status("Proposed")
                .build();

        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void shouldReadViewElements() throws Exception {
        URL resource = getClass().getResource(TestHelper.JSON_STRUCTURIZR_BIG_BANK);
        ArchitectureDataStructure dataStructure = new WorkspaceReader().load(new File(resource.getPath()));

        final Set<C4Reference> actual = dataStructure.getViews().getComponentViews().get(0).getElements();
        var expected = new HashSet<C4Reference>();
        expected.add(new C4Reference("4", null));
        expected.add(new C4Reference("6", null));
        expected.add(new C4Reference("17", null));
        expected.add(new C4Reference("18", null));
        expected.add(new C4Reference("21", null));
        expected.add(new C4Reference("29", null));
        expected.add(new C4Reference("30", null));
        expected.add(new C4Reference("31", null));
        expected.add(new C4Reference("32", null));
        expected.add(new C4Reference("33", null));
        expected.add(new C4Reference("34", null));

        assertThat(actual, is(expected));
    }
}
