package net.trilogy.arch.adapter;

import lombok.NonNull;
import net.trilogy.arch.TestHelper;
import net.trilogy.arch.adapter.structurizr.WorkspaceReader;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.DocumentationImage;
import net.trilogy.arch.domain.ImportantTechnicalDecision;
import net.trilogy.arch.domain.c4.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.io.File;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static net.trilogy.arch.domain.c4.C4Action.USES;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;


public class WorkspaceReaderTest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();

    @Test
    public void shouldHaveValidDescription() throws Exception {
        URL resource = getClass().getResource(TestHelper.JSON_STRUCTURIZR_EMPTY);
        ArchitectureDataStructure dataStructure = new WorkspaceReader().load(new File(resource.getPath()));

        collector.checkThat(dataStructure.getDescription(), equalTo(""));
    }

    @Test
    public void shouldReadComponent() throws Exception {
        URL resource = getClass().getResource(TestHelper.JSON_STRUCTURIZR_THINK3_SOCOCO);
        ArchitectureDataStructure data = new WorkspaceReader().load(new File(resource.getPath()));

        C4Component component = (C4Component) data.getModel().findEntityById("220").orElseThrow(() -> new IllegalStateException("Could not find entity with id: " + "220"));

        collector.checkThat(component.getName(), is(equalTo("Ionic")));
        collector.checkThat(component.getContainerAlias(), is(nullValue()));
        collector.checkThat(component.getAlias(), is(nullValue()));
        collector.checkThat(component.getTags(), containsInAnyOrder(
                new C4Tag("Element"),
                new C4Tag("Component"),
                new C4Tag("External")
        ));
        collector.checkThat(component.getDescription(), is(equalTo("Ionic native part for Android")));
        collector.checkThat(component.getContainerId(), is(equalTo("219")));
        collector.checkThat(component.getType(), is(equalTo(C4Type.COMPONENT)));
        collector.checkThat(component.getUrl(), is(nullValue()));
        collector.checkThat(component.getPath(), is(C4Path.path("c4://Sococo Virtual Office/Android App/Ionic")));
        collector.checkThat(component.getRelationships(), contains(
                new C4Relationship("239", null, USES, null, "16", "Runs", "Chromium")
        ));
        collector.checkThat(component.getTechnology(), is(equalTo("Android")));
        collector.checkThat(component.getSrcMappings(), contains("src/bin/bash", "src/bin/zsh"));
    }

    @Test
    public void shouldReadCorrectNumberOfElements() throws Exception {
        URL resource = getClass().getResource(TestHelper.JSON_STRUCTURIZR_THINK3_SOCOCO);
        ArchitectureDataStructure dataStructure = new WorkspaceReader().load(new File(resource.getPath()));

        collector.checkThat(dataStructure.getName(), is(equalTo("Sococo Import")));
        collector.checkThat(dataStructure.getBusinessUnit(), is(equalTo("Think3")));

        collector.checkThat(dataStructure, is(notNullValue()));
        collector.checkThat(dataStructure.getModel(), is(notNullValue()));
        collector.checkThat(dataStructure.getModel().getPeople(), hasSize(7));
        collector.checkThat(dataStructure.getModel().getSystems(), hasSize(13));
        collector.checkThat(dataStructure.getModel().getContainers(), hasSize(28));
        collector.checkThat(dataStructure.getModel().getComponents(), hasSize(35));
        collector.checkThat(dataStructure.getModel().allRelationships(), hasSize(160));
    }

    @Test
    public void shouldReadDeploymentNodes() throws Exception {
        URL resource = getClass().getResource(TestHelper.JSON_STRUCTURIZR_BIG_BANK);
        ArchitectureDataStructure dataStructure = new WorkspaceReader().load(new File(resource.getPath()));

        collector.checkThat(dataStructure.getModel().getDeploymentNodes().size(), is(equalTo(4)));
        collector.checkThat(dataStructure.getModel().getDeploymentNodesRecursively().size(), is(equalTo(18)));

        var actual = (C4DeploymentNode) dataStructure.getModel().findEntityById("65").orElseThrow(() -> new IllegalStateException("Could not find entity with id: " + "65"));

        var expected = C4DeploymentNode.builder()
                .alias(null)
                .id("65")
                .name("Customer's computer")
                .children(Set.of(
                        C4DeploymentNode.builder()
                                .id("66")
                                .name("Web Browser")
                                .environment("Live")
                                .technology("Chrome, Firefox, Safari, or Edge")
                                .instances(1)
                                .containerInstances(Set.of(
                                        new C4ContainerInstance("67", "Live", new C4Reference("17", null), 2)
                                ))
                                .children(Set.of())
                                .build()
                ))
                .containerInstances(Set.of())
                .environment("Live")
                .technology("Microsoft Windows or Apple macOS")
                .instances(1)
                .tags(Set.of())
                .relationships(List.of())
                .build();

        collector.checkThat(actual, is(equalTo(expected)));
    }

    @Test
    public void shouldReadDecisions() throws Exception {
        URL resource = getClass().getResource(TestHelper.JSON_STRUCTURIZR_THINK3_SOCOCO);
        String id = "4";

        ArchitectureDataStructure dataStructure = new WorkspaceReader().load(new File(resource.getPath()));
        List<ImportantTechnicalDecision> decisions = dataStructure.getDecisions();

        collector.checkThat(decisions.size(), is(equalTo(4)));
        ImportantTechnicalDecision actual = decisions.stream().filter(d -> d.getId().equals(id)).findFirst().get();

        ImportantTechnicalDecision expected = ImportantTechnicalDecision.builder()
                .id(id)
                .title("Deploy Single Page Application container build onto CDN")
                .date(Date.from(LocalDateTime.parse("2020-01-12T09:36:04Z", ISO_DATE_TIME).toInstant(UTC)))
                .content("## Context\nSingle Page Application is written in Angular and supports webpack builds.\n\n## Decision\nTBD\n\n## Consequences")
                .status("Proposed")
                .build();

        collector.checkThat(actual, is(equalTo(expected)));
    }

    @Test
    public void shouldReadDocumentation() throws Exception {
        URL resource = getClass().getResource(TestHelper.JSON_STRUCTURIZR_BIG_BANK);
        ArchitectureDataStructure dataStructure = new WorkspaceReader().load(new File(resource.getPath()));

        final int size = dataStructure.getDocumentation().size();
        final Set<String> titles = dataStructure.getDocumentation().stream()
                .map(doc -> doc.getTitle()).collect(Collectors.toSet());

        collector.checkThat(size, equalTo(5));
        collector.checkThat(titles, containsInAnyOrder("Context", "Components", "Development Environment", "Containers", "Deployment"));
    }

    @Test
    public void shouldReadDocumentationImages() throws Exception {
        URL resource = getClass().getResource(TestHelper.JSON_STRUCTURIZR_EMBEDDED_IMAGE);
        ArchitectureDataStructure dataStructure = new WorkspaceReader().load(new File(resource.getPath()));

        @NonNull List<DocumentationImage> documentationImages = dataStructure.getDocumentationImages();

        collector.checkThat(documentationImages.size(), equalTo(1));
        collector.checkThat(documentationImages.get(0).getName(), equalTo("thoughtworks.png"));
        collector.checkThat(documentationImages.get(0).getContent(), equalTo("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"));
        collector.checkThat(documentationImages.get(0).getType(), equalTo("image/png"));
    }
}
