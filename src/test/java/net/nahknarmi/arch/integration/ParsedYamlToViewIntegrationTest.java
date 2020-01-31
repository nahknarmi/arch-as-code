package net.nahknarmi.arch.integration;

import com.structurizr.Workspace;
import com.structurizr.view.ComponentView;
import com.structurizr.view.ContainerView;
import com.structurizr.view.SystemContextView;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.ArchitectureDataStructureReader;
import net.nahknarmi.arch.transformation.ArchitectureDataStructureTransformer;
import net.nahknarmi.arch.transformation.TransformerFactory;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static net.nahknarmi.arch.TestHelper.TEST_VIEW_ROOT_PATH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

public class ParsedYamlToViewIntegrationTest {

    @Test
    public void system_view_test() throws IOException {
        Workspace workspace = getWorkspace();
        Collection<SystemContextView> systemContextViews = workspace.getViews().getSystemContextViews();
        SystemContextView view = systemContextViews.stream().findFirst().get();
        List<String> elementNames = view.getElements().stream().map(e -> e.getElement().getName()).collect(Collectors.toList());

        assertThat(view.getRelationships().size(), equalTo(4));
        assertThat(elementNames,
                containsInAnyOrder("Personal Banking Customer",
                        "E-mail System",
                        "Mainframe Banking System",
                        "Internet Banking System"
                ));
    }

    @Test
    public void container_view_test() throws IOException {
        Workspace workspace = getWorkspace();
        Collection<ContainerView> containerViews = workspace.getViews().getContainerViews();
        ContainerView view = containerViews.stream().findFirst().get();
        List<String> elementNames = view.getElements().stream().map(e -> e.getElement().getName()).collect(Collectors.toList());

        assertThat(view.getRelationships().size(), equalTo(10));
        assertThat(elementNames,
                containsInAnyOrder("Personal Banking Customer",
                        "E-mail System",
                        "Mainframe Banking System",
                        "API Application",
                        "Database",
                        "Web Application",
                        "Single-Page Application",
                        "Mobile App"
                ));
    }

    @Test
    public void component_view_test() throws IOException {
        Workspace workspace = getWorkspace();
        Collection<ComponentView> componentViews = workspace.getViews().getComponentViews();
        ComponentView view = componentViews.stream().findFirst().get();
        List<String> elementNames = view.getElements().stream().map(e -> e.getElement().getName()).collect(Collectors.toList());

        assertThat(view.getRelationships().size(), equalTo(13));
        assertThat(elementNames,
                containsInAnyOrder("E-mail System",
                        "Mainframe Banking System",
                        "Database",
                        "Single-Page Application",
                        "Mobile App",
                        "Sign In Controller",
                        "Reset Password Controller",
                        "Accounts Summary Controller",
                        "Mainframe Banking System Facade",
                        "E-mail Component",
                        "Security Component"
                ));
    }

    private Workspace getWorkspace() throws IOException {
        File documentationRoot = new File(getClass().getResource(TEST_VIEW_ROOT_PATH).getPath());
        File manifestFile = new File(documentationRoot + File.separator + "data-structure.yml");

        ArchitectureDataStructure dataStructure = new ArchitectureDataStructureReader().load(manifestFile);
        ArchitectureDataStructureTransformer transformer = TransformerFactory.create(documentationRoot);
        return transformer.toWorkSpace(dataStructure);
    }
}
