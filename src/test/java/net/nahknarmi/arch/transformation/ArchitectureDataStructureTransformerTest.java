package net.nahknarmi.arch.transformation;

import com.google.common.collect.ImmutableList;
import com.structurizr.Workspace;
import com.structurizr.documentation.Decision;
import com.structurizr.documentation.DecisionStatus;
import net.nahknarmi.arch.model.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import static java.util.Collections.emptyList;
import static net.nahknarmi.arch.TestHelper.TEST_PRODUCT_DOCUMENTATION_ROOT_PATH;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class ArchitectureDataStructureTransformerTest {
    private static final String PRODUCT_NAME = "testspaces";
    private static final String PRODUCT_DESCRIPTION = "DevSpaces is a tool";

    @Test
    public void should_transform_architecture_yaml_to_structurizr_workspace() throws IOException {
        ArchitectureDataStructure dataStructure =
                new ArchitectureDataStructure(PRODUCT_NAME, 1L, "DevFactory", PRODUCT_DESCRIPTION, emptyList(), buildModel());

        File documentationRoot = new File(getClass().getResource(TEST_PRODUCT_DOCUMENTATION_ROOT_PATH).getPath());
        ArchitectureDataStructureTransformer transformer = new ArchitectureDataStructureTransformer(documentationRoot);
        Workspace workspace = transformer.toWorkSpace(dataStructure);

        assertNotNull(workspace);
        assertThat(workspace.getId(), equalTo(1L));
        assertThat(workspace.getName(), equalTo(PRODUCT_NAME));
        assertThat(workspace.getDescription(), equalTo(PRODUCT_DESCRIPTION));
        assertThat(workspace.getDocumentation().getSections().size(), equalTo(2));
        assertThat(workspace.getModel().getPeople().size(), equalTo(1));
        assertThat(workspace.getModel().getSoftwareSystems().size(), equalTo(1));
    }

    @Test
    public void should_tranform_accept_decision_status() throws IOException {
        checkStatus(DecisionStatus.Accepted);
    }

    @Test
    public void should_tranform_superseded_decision_status() throws IOException {
        checkStatus(DecisionStatus.Superseded);
    }

    @Test
    public void should_tranform_deprecated_decision_status() throws IOException {
        checkStatus(DecisionStatus.Deprecated);
    }

    @Test
    public void should_tranform_rejected_decision_status() throws IOException {
        checkStatus(DecisionStatus.Rejected);
    }

    @Test
    public void should_tranform_proposed_decision_status() throws IOException {
        checkStatus(DecisionStatus.Proposed);
    }

    @Test
    public void should_tranform_decision_status_to_default() throws IOException {
        checkStatus(DecisionStatus.Proposed, "Something invalid");
    }

    private void checkStatus(DecisionStatus decisionStatus) throws IOException {
        checkStatus(decisionStatus, decisionStatus.name());
    }

    private void checkStatus(DecisionStatus decisionStatus, String statusString) throws IOException {
        ArchitectureDataStructure dataStructure =
                new ArchitectureDataStructure(PRODUCT_NAME, 1L, "DevFactory", PRODUCT_DESCRIPTION,
                        ImmutableList.of(new ImportantTechnicalDecision("1", new Date(), "title", statusString, "content")), buildModel());

        File documentationRoot = new File(getClass().getResource(TEST_PRODUCT_DOCUMENTATION_ROOT_PATH).getPath());
        ArchitectureDataStructureTransformer transformer = new ArchitectureDataStructureTransformer(documentationRoot);
        Workspace workspace = transformer.toWorkSpace(dataStructure);

        ArrayList<Decision> decisions = new ArrayList<>(workspace.getDocumentation().getDecisions());
        DecisionStatus result = decisions.get(0).getStatus();

        assertThat(result, equalTo(decisionStatus));
    }

    private C4Model buildModel() {
        return new C4Model(
                ImmutableList.of(new C4Person("Foo", "Bar", emptyList())),
                ImmutableList.of(new C4SoftwareSystem("J2EE Server", "Application server", emptyList())));
    }

    //handle id being absent, name, description.

}
