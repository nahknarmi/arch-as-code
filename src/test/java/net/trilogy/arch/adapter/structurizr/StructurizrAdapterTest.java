package net.trilogy.arch.adapter.structurizr;


import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClient;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

public class StructurizrAdapterTest {

    @Test
    public void shouldUseClientProvided() {
        StructurizrClient client = new StructurizrClient("url", "api-key", "api-secret");

        StructurizrAdapter structurizrAdapter = new StructurizrAdapter(client);

        assertThat(structurizrAdapter.getClient(), is(client));
    }

    @Test
    public void shouldPublishWithClient() throws Exception {
        // Given
        Workspace workspace = new Workspace("name", "desc");
        StructurizrClient client = mock(StructurizrClient.class);
        StructurizrAdapter structurizrAdapter = new StructurizrAdapter(client);

        // When
        structurizrAdapter.publish(workspace);

        // Then
        verify(client).putWorkspace(anyLong(), any(Workspace.class));
    }

    @Test
    public void shouldReturnTrueIfPublishSucceeded() {
        // Given
        Workspace workspace = new Workspace("name", "desc");
        StructurizrClient client = mock(StructurizrClient.class);
        StructurizrAdapter structurizrAdapter = new StructurizrAdapter(client);

        // When
        Boolean result = structurizrAdapter.publish(workspace);

        // Then
        assertThat(result, equalTo(true));
    }

    @Test
    public void shouldReturnFalseIfPublishFailed() throws Exception {
        // Given
        Workspace workspace = new Workspace("name", "desc");
        StructurizrClient client = mock(StructurizrClient.class);
        doThrow(new RuntimeException("Boom!")).when(client).putWorkspace(anyLong(), any(Workspace.class));
        StructurizrAdapter structurizrAdapter = new StructurizrAdapter(client);

        // When
        Boolean result = structurizrAdapter.publish(workspace);

        // Then
        assertThat(result, equalTo(false));
    }

}
