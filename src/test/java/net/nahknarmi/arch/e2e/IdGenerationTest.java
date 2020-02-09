package net.nahknarmi.arch.e2e;

import com.structurizr.Workspace;
import net.nahknarmi.arch.adapter.in.ArchitectureDataStructureReader;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.Entity;
import net.nahknarmi.arch.transformation.ArchitectureDataStructureTransformer;
import net.nahknarmi.arch.transformation.TransformerFactory;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static net.nahknarmi.arch.TestHelper.TEST_PRODUCT_DOCUMENTATION_ROOT_PATH;
import static net.nahknarmi.arch.domain.c4.C4Path.path;
import static org.junit.Assert.assertEquals;

public class IdGenerationTest {

    @Test
    public void people_id_generation_test() throws IOException {
        ArchitectureDataStructure dataStructure = getDataStructure();
        Workspace workspace = getWorkspace(dataStructure);

        workspace.getModel().getPeople().forEach(p -> {
            String personId = p.getId();
            Entity entity = dataStructure.getModel().findByPath(path(personId));
            String pathString = entity.getPath().getPath();

            assertEquals(personId, pathString);
        });
    }

    @Test
    public void systems_id_generation_test() throws IOException {
        ArchitectureDataStructure dataStructure = getDataStructure();
        Workspace workspace = getWorkspace(dataStructure);

        workspace.getModel().getSoftwareSystems().forEach(s -> {
            String systemId = s.getId();
            Entity entity = dataStructure.getModel().findByPath(path(systemId));
            String pathString = entity.getPath().getPath();

            assertEquals(systemId, pathString);
        });
    }

    @Test
    public void containers_id_generation_test() throws IOException {
        ArchitectureDataStructure dataStructure = getDataStructure();
        Workspace workspace = getWorkspace(dataStructure);

        workspace.getModel().getSoftwareSystems().forEach(system -> {
            system.getContainers().forEach(cont -> {
                String containerId = cont.getId();
                Entity entity = dataStructure.getModel().findByPath(path(containerId));
                String pathString = entity.getPath().getPath();

                assertEquals(containerId, pathString);
            });
        });
    }

    @Test
    public void component_id_generation_test() throws IOException {
        ArchitectureDataStructure dataStructure = getDataStructure();
        Workspace workspace = getWorkspace(dataStructure);

        workspace.getModel().getSoftwareSystems().forEach(system -> {
            system.getContainers().forEach(container -> {
                container.getComponents().forEach(comp -> {
                    String componentId = comp.getId();
                    Entity entity = dataStructure.getModel().findByPath(path(componentId));
                    String pathString = entity.getPath().getPath();

                    assertEquals(componentId, pathString);
                });
            });
        });
    }

    private ArchitectureDataStructure getDataStructure() throws IOException {
        File documentationRoot = new File(getClass().getResource(TEST_PRODUCT_DOCUMENTATION_ROOT_PATH).getPath());
        File manifestFile = new File(documentationRoot + File.separator + "data-structure.yml");
        return new ArchitectureDataStructureReader().load(manifestFile);
    }

    private Workspace getWorkspace(ArchitectureDataStructure dataStructure) {
        File documentationRoot = new File(getClass().getResource(TEST_PRODUCT_DOCUMENTATION_ROOT_PATH).getPath());
        ArchitectureDataStructureTransformer transformer = TransformerFactory.create(documentationRoot);
        return transformer.toWorkSpace(dataStructure);
    }
}
