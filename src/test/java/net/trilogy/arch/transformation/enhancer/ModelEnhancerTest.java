package net.trilogy.arch.transformation.enhancer;

import com.google.common.collect.ImmutableSet;
import com.structurizr.Workspace;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.c4.C4Model;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ModelEnhancerTest {
    @Test
    public void shouldEnhance() {
        Workspace workspace = new Workspace("foo", "bazz");
        ArchitectureDataStructure dataStructure = mock(ArchitectureDataStructure.class);
        C4Model model = mock(C4Model.class);

        when(dataStructure.getModel()).thenReturn(model);
        when(model.getPeople()).thenReturn(ImmutableSet.of());

        new ModelEnhancer().enhance(workspace, dataStructure);

        assertThat(workspace.getModel().getPeople(), hasSize(0));
    }

    @Test
    public void enhance() {
        Workspace workspace = new Workspace("foo", "bazz");
        ArchitectureDataStructure dataStructure = mock(ArchitectureDataStructure.class);
        C4Model model = mock(C4Model.class);

        when(dataStructure.getModel()).thenReturn(model);
        when(model.getPeople()).thenReturn(ImmutableSet.of());

        new ModelEnhancer().enhance(workspace, dataStructure);

        assertThat(workspace.getModel().getPeople(), hasSize(0));
    }
}
