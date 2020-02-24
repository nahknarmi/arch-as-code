package net.nahknarmi.arch.adapter;


import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClientException;
import org.junit.Test;

import static net.nahknarmi.arch.TestHelper.TEST_WORKSPACE_ID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class StructurizrAdapterTest {

    @Test
    public void should_bump_structurizr_revision_after_publishing() throws StructurizrClientException {
        StructurizrAdapter adapter = new StructurizrAdapter();
        Workspace workspace = adapter.load(TEST_WORKSPACE_ID);
        Long revision = workspace.getRevision();

        //when
        adapter.publish(workspace);

        //then
        Workspace updatedWorkspace = new StructurizrAdapter().load(TEST_WORKSPACE_ID);
        assertThat(updatedWorkspace.getRevision(), is(equalTo(revision + 1)));
    }

}
