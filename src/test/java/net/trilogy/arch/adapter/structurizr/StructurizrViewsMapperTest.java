package net.trilogy.arch.adapter.structurizr;

import com.structurizr.Workspace;
import com.structurizr.view.*;
import net.trilogy.arch.TestHelper;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.facade.FilesFacade;
import net.trilogy.arch.publish.ArchitectureDataStructurePublisher;
import net.trilogy.arch.transformation.ArchitectureDataStructureTransformer;
import net.trilogy.arch.transformation.TransformerFactory;
import org.junit.Test;

import java.io.File;
import java.util.Collection;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class StructurizrViewsMapperTest {

    @Test
    public void shouldLoadAndSetViews() {
        File productArchitectureDir = new File(getClass().getResource(TestHelper.ROOT_PATH_TO_TEST_GENERALLY).getPath());
        Workspace workspace = new Workspace("name", "desc");

        StructurizrViewsMapper mapper = new StructurizrViewsMapper(productArchitectureDir);
        mapper.loadAndSetViews(workspace);

        ViewSet views = workspace.getViews();
        assertThat(views, notNullValue());
        Collection<SystemContextView> systemViews = views.getSystemContextViews();
        assertThat(systemViews.size(), equalTo(2));
        assertThat(systemViews.stream().map(View::getSoftwareSystemId).collect(toList()), hasItems("7","9"));

        Collection<ContainerView> containerViews = views.getContainerViews();
        assertThat(containerViews.size(), equalTo(1));
        assertThat(containerViews.stream().map(View::getSoftwareSystemId).collect(toList()), hasItem("9"));

        Collection<ComponentView> componentViews = views.getComponentViews();
        assertThat(componentViews.size(), equalTo(1));
        assertThat(componentViews.stream().map(ComponentView::getContainerId).collect(toList()), hasItem("13"));
    }

    @Test
    public void shouldSetReferencedObjectsOnViews() throws Exception {
        File documentationRoot = new File(getClass().getResource(TestHelper.ROOT_PATH_TO_TEST_PRODUCT_DOCUMENTATION).getPath());

        ArchitectureDataStructurePublisher publisher = new ArchitectureDataStructurePublisher(new FilesFacade(), documentationRoot, "");
        ArchitectureDataStructure dataStructure = publisher.loadProductArchitecture(documentationRoot, "product-architecture.yml");

        ArchitectureDataStructureTransformer transformer = TransformerFactory.create(documentationRoot);
        Workspace workspace = transformer.toWorkSpace(dataStructure);

        ViewSet views = workspace.getViews();
        views.getSystemContextViews().forEach(sv -> assertThat(sv.getSoftwareSystem().getId(), equalTo(sv.getSoftwareSystemId())));
        views.getContainerViews().forEach(cv -> assertThat(cv.getSoftwareSystem().getId(), equalTo(cv.getSoftwareSystemId())));
        views.getComponentViews().forEach(cv -> assertThat(cv.getSoftwareSystem().getId(), equalTo(cv.getSoftwareSystemId())));
        views.getComponentViews().forEach(cv -> assertThat(cv.getContainer().getId(), equalTo(cv.getContainerId())));
    }
}
