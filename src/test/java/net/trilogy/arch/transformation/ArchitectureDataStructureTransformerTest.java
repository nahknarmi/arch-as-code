package net.trilogy.arch.transformation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.structurizr.Workspace;
import com.structurizr.documentation.Decision;
import com.structurizr.documentation.DecisionStatus;
import com.structurizr.model.Component;
import com.structurizr.model.Container;
import com.structurizr.model.Model;
import net.trilogy.arch.TestHelper;
import net.trilogy.arch.adapter.architectureYaml.ArchitectureDataStructureReader;
import net.trilogy.arch.adapter.structurizr.WorkspaceReader;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.ImportantTechnicalDecision;
import net.trilogy.arch.domain.c4.C4Model;
import net.trilogy.arch.domain.c4.C4Person;
import net.trilogy.arch.domain.c4.C4SoftwareSystem;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static net.trilogy.arch.domain.c4.C4Location.INTERNAL;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertNotNull;

public class ArchitectureDataStructureTransformerTest {
    private static final String PRODUCT_NAME = "TestSpaces";
    private static final String PRODUCT_DESCRIPTION = "TestSpaces is a tool!";

    @Test
    public void shouldHandleMultipleRelationshipsWithSameSouceAndDestination() throws Exception {
        ArchitectureDataStructureTransformer transformer = getTransformer(TestHelper.ROOT_PATH_TO_TEST_VIEWS);
        File structurizrJson = new File(getClass().getResource(TestHelper.JSON_STRUCTURIZR_MULTIPLE_RELATIONSHIPS).getPath());
        ArchitectureDataStructure ourYaml = new WorkspaceReader().load(structurizrJson);

        Workspace exportedJson = transformer.toWorkSpace(ourYaml);

        List<String> relationshipIds = exportedJson.getModel().getRelationships().stream().map(rel -> rel.getId()).sorted().collect(Collectors.toList());

        System.out.println(exportedJson.getModel().getRelationships());

        System.out.println(relationshipIds);

        assertThat(relationshipIds, contains("10", "6->7:1", "6->7:2", "8"));
    }

    @Test
    public void should_transform_a_json_with_tricky_deployment_node_scopes() throws Exception {
        // given
        ArchitectureDataStructureTransformer transformer = getTransformer(TestHelper.ROOT_PATH_TO_TEST_VIEWS);
        File jsonFromStructurizr = new File(getClass().getResource(TestHelper.JSON_STRUCTURIZR_TRICKY_DEPLOYMENT_NODE_SCOPES).getPath());
        ArchitectureDataStructure ourDataStructure = new WorkspaceReader().load(jsonFromStructurizr);

        // when
        Workspace workspace = transformer.toWorkSpace(ourDataStructure);

        // then
        assertThat(workspace.getModel().getDeploymentNodes().size(), equalTo(3));
    }

    private ArchitectureDataStructureTransformer getTransformer(String rootPathToTestViews) {
        File documentationRoot = new File(getClass().getResource(rootPathToTestViews).getPath());
        return TransformerFactory.create(documentationRoot);
    }

    @Test
    public void should_transform_architecture_yaml_to_structurizr_workspace() throws Exception {
        File documentationRoot = new File(getClass().getResource(TestHelper.ROOT_PATH_TO_TEST_PRODUCT_DOCUMENTATION).getPath());
        File manifestFile = new File(getClass().getResource(TestHelper.MANIFEST_PATH_TO_TEST_GENERALLY).getPath());

        ArchitectureDataStructure dataStructure = new ArchitectureDataStructureReader(new FilesFacade()).load(manifestFile);

        ArchitectureDataStructureTransformer transformer = TransformerFactory.create(documentationRoot);
        Workspace workspace = transformer.toWorkSpace(dataStructure);

        assertNotNull(workspace);
        assertThat(workspace.getName(), equalTo(PRODUCT_NAME));
        assertThat(workspace.getDescription(), equalTo(PRODUCT_DESCRIPTION));
        assertThat(workspace.getDocumentation().getSections().size(), equalTo(4));
        Model model = workspace.getModel();
        assertThat(model.getPeople().size(), equalTo(4));
        assertThat(model.getSoftwareSystems().size(), equalTo(5));
        Container container = (Container) model.getElement("12"); // "c4://DevSpaces/DevSpaces Web Application"
        assertThat(container, notNullValue());
        assertThat(container.getUrl(), equalTo("https://devspaces.io"));
        Component component = (Component) model.getElement("38"); // "c4://DevSpaces/DevSpaces API/Sign In Controller"
        assertThat(component, notNullValue());
        assertThat(component.getUrl(), equalTo("https://devspaces.io/sign-in"));
        assertThat(component.getProperties().get("Source Code Mappings"), equalTo(
                "[\"src/bin/bash\",\"src/bin/zsh\"]"
        ));
    }

    @Test
    public void should_tranform_accept_decision_status() {
        checkStatus(DecisionStatus.Accepted);
    }

    @Test
    public void should_tranform_superseded_decision_status() {
        checkStatus(DecisionStatus.Superseded);
    }

    @Test
    public void should_tranform_deprecated_decision_status() {
        checkStatus(DecisionStatus.Deprecated);
    }

    @Test
    public void should_tranform_rejected_decision_status() {
        checkStatus(DecisionStatus.Rejected);
    }

    @Test
    public void should_tranform_proposed_decision_status() {
        checkStatus(DecisionStatus.Proposed);
    }

    @Test
    public void should_tranform_decision_status_to_default() {
        checkStatus(DecisionStatus.Proposed, "Something invalid");
    }

    private void checkStatus(DecisionStatus decisionStatus) {
        checkStatus(decisionStatus, decisionStatus.name());
    }

    private void checkStatus(DecisionStatus decisionStatus, String statusString) {
        ArchitectureDataStructure dataStructure =
                ArchitectureDataStructure.builder()
                        .name(PRODUCT_NAME)
                        .businessUnit("DevFactory")
                        .description(PRODUCT_DESCRIPTION)
                        .model(buildModel())
                        .decisions(ImmutableList.of(new ImportantTechnicalDecision("1", new Date(), "title", statusString, "content")))
                        .build();

        ArchitectureDataStructureTransformer transformer = getTransformer(TestHelper.ROOT_PATH_TO_TEST_PRODUCT_DOCUMENTATION);
        Workspace workspace = transformer.toWorkSpace(dataStructure);

        ArrayList<Decision> decisions = new ArrayList<>(workspace.getDocumentation().getDecisions());
        DecisionStatus result = decisions.get(0).getStatus();

        assertThat(result, equalTo(decisionStatus));
    }

    private C4Model buildModel() {

        return new C4Model(
                ImmutableSet.of(
                        C4Person.builder()
                                .id("1")
                                .alias("@person")
                                .name("person")
                                .description("Foo")
                                .relationships(emptyList()).tags(emptySet()).build()
                ),
                ImmutableSet.of(
                        C4SoftwareSystem.builder()
                                .id("2")
                                .alias("c4://sys")
                                .name("sys")
                                .description("sys")
                                .location(INTERNAL)
                                .tags(emptySet())
                                .relationships(emptyList()).build()
                ),
                emptySet(),
                emptySet(),
                emptySet()
        );
    }
}
