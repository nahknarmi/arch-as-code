package net.trilogy.arch.transformation.enhancer;

import com.structurizr.Workspace;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.ImportantTechnicalDecision;
import org.junit.Test;

import java.util.Date;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DecisionEnhancerTest {

    @Test
    public void no_decisions_when_empty_decision_list() {
        Workspace workspace = new Workspace("foo", "bar");
        ArchitectureDataStructure dataStructure = mock(ArchitectureDataStructure.class);

        when(dataStructure.getDecisions()).thenReturn(emptyList());

        new DecisionEnhancer().enhance(workspace, dataStructure);

        assertEquals(0, workspace.getDocumentation().getDecisions().size());
    }

    @Test
    public void one_decision_when_single_decision_list() {
        Workspace workspace = new Workspace("foo", "bar");
        ArchitectureDataStructure dataStructure = mock(ArchitectureDataStructure.class);

        when(dataStructure.getDecisions()).thenReturn(of(new ImportantTechnicalDecision("1", new Date(), "title", "", "##Some content")));

        new DecisionEnhancer().enhance(workspace, dataStructure);

        assertEquals(1, workspace.getDocumentation().getDecisions().size());
    }

    @Test
    public void two_decision_when_two_decision_list() {
        Workspace workspace = new Workspace("foo", "bar");
        ArchitectureDataStructure dataStructure = mock(ArchitectureDataStructure.class);

        when(dataStructure.getDecisions())
                .thenReturn(of(
                        new ImportantTechnicalDecision("1", new Date(), "title", "", "##Some content"),
                        new ImportantTechnicalDecision("2", new Date(), "title", "", "##Some content"))
                );

        new DecisionEnhancer().enhance(workspace, dataStructure);

        assertEquals(2, workspace.getDocumentation().getDecisions().size());
    }
}
