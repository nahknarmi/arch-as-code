package net.trilogy.arch.e2e;

import com.structurizr.Workspace;
import com.structurizr.model.Element;
import net.trilogy.arch.TestHelper;
import net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureReader;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.facade.FilesFacade;
import net.trilogy.arch.transformation.ArchitectureDataStructureTransformer;
import net.trilogy.arch.transformation.TransformerFactory;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TddIdGenerationTest {

    @Test
    public void people_id_generation_test() throws Exception {
        ArchitectureDataStructure dataStructure = getDataStructure();
        Workspace workspace = getWorkspace(dataStructure);

        dataStructure.getModel().getPeople().forEach(p -> {
            String personId = p.getId();
            Element element = workspace.getModel().getElement(personId);
            String elementId = element.getId();

            assertEquals(personId, elementId);
        });
    }

    @Test
    public void systems_id_generation_test() throws Exception {
        ArchitectureDataStructure dataStructure = getDataStructure();
        Workspace workspace = getWorkspace(dataStructure);

        dataStructure.getModel().getSystems().forEach(s -> {
            String systemId = s.getId();
            Element element = workspace.getModel().getElement(systemId);
            String elementId = element.getId();

            assertEquals(systemId, elementId);
        });
    }

    @Test
    public void containers_id_generation_test() throws Exception {
        ArchitectureDataStructure dataStructure = getDataStructure();
        Workspace workspace = getWorkspace(dataStructure);

        dataStructure.getModel().getContainers().forEach(cont -> {
            String containerId = cont.getId();
            Element element = workspace.getModel().getElement(containerId);
            String elementId = element.getId();

            assertEquals(containerId, elementId);
        });
    }

    @Test
    public void component_id_generation_test() throws Exception {
        ArchitectureDataStructure dataStructure = getDataStructure();
        Workspace workspace = getWorkspace(dataStructure);

        dataStructure.getModel().getComponents().forEach(comp -> {
            String componentId = comp.getId();
            Element element = workspace.getModel().getElement(componentId);
            String elementId = element.getId();

            assertEquals(componentId, elementId);
        });
    }

    @SuppressWarnings("deprecation")
    private ArchitectureDataStructure getDataStructure() throws IOException {
        File documentationRoot = new File(getClass().getResource(TestHelper.ROOT_PATH_TO_TEST_PRODUCT_DOCUMENTATION).getPath());
        File manifestFile = new File(documentationRoot + File.separator + "product-architecture.yml");
        return new ArchitectureDataStructureReader(new FilesFacade()).loadArchitectureDataStructure(manifestFile);
    }

    private Workspace getWorkspace(ArchitectureDataStructure dataStructure) {
        File documentationRoot = new File(getClass().getResource(TestHelper.ROOT_PATH_TO_TEST_PRODUCT_DOCUMENTATION).getPath());
        ArchitectureDataStructureTransformer transformer = TransformerFactory.create(documentationRoot);
        return transformer.toWorkSpace(dataStructure);
    }
}
