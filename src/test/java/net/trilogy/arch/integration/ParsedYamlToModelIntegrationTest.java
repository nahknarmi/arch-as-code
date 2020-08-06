package net.trilogy.arch.integration;

import com.structurizr.Workspace;
import com.structurizr.model.*;
import net.trilogy.arch.TestHelper;
import net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureReader;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.facade.FilesFacade;
import net.trilogy.arch.transformation.ArchitectureDataStructureTransformer;
import net.trilogy.arch.transformation.TransformerFactory;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

public class ParsedYamlToModelIntegrationTest {

    @Test
    public void should_build_person_developer() throws Exception {
        String personName = "Developer";
        Workspace workspace = getWorkspace();

        Person person = workspace.getModel().getPersonWithName(personName);
        Set<String> tagSet = person.getTagsAsSet();
        Set<Relationship> relationships = person.getRelationships();
        List<String> relationshipNames = relationships.stream().map(r -> r.getDestination().getName()).collect(Collectors.toList());

        assertTrue(tagSet.contains("DevSpaces System View"));
        assertTrue(tagSet.contains("Trilogy System View"));
        assertTrue(tagSet.contains("DevSpaces Container View"));

        assertThat(relationships, hasSize(5));
        assertTrue(relationshipNames.contains("GitHub"));
        assertTrue(relationshipNames.contains("DevSpaces"));
        assertTrue(relationshipNames.contains("DevSpaces/DevSpaces CLI"));
        assertTrue(relationshipNames.contains("Trilogy Google G Suite"));
        assertTrue(relationshipNames.contains("DevSpaces/DevSpaces API/Sign In Controller"));

        assertEquals(person.getDescription(), "Developer building software");
        assertEquals(person.getLocation(), Location.Internal);
    }

    @Test
    public void should_build_person_saasops() throws Exception {
        String personName = "SaasOps";
        Workspace workspace = getWorkspace();

        Person person = workspace.getModel().getPersonWithName(personName);
        Set<String> tagSet = person.getTagsAsSet();
        Set<Relationship> relationships = person.getRelationships();
        List<String> relationshipNames = relationships.stream().map(r -> r.getDestination().getName()).collect(Collectors.toList());

        assertTrue(tagSet.contains("DevSpaces System View"));
        assertTrue(tagSet.contains("Trilogy System View"));
        assertTrue(tagSet.contains("DevSpaces Container View"));

        assertThat(relationships, hasSize(4));
        assertTrue(relationshipNames.contains("GitHub"));
        assertTrue(relationshipNames.contains("DevSpaces"));
        assertTrue(relationshipNames.contains("DevSpaces/DevSpaces Web Application"));
        assertTrue(relationshipNames.contains("Trilogy Google G Suite"));
        assertEquals(person.getDescription(), "SaasOps operating system");
    }

    @Test
    public void should_build_person_pca() throws Exception {
        String personName = "PCA";
        Workspace workspace = getWorkspace();

        Person person = workspace.getModel().getPersonWithName(personName);
        Set<String> tagSet = person.getTagsAsSet();
        Set<Relationship> relationships = person.getRelationships();
        List<String> relationshipNames = relationships.stream().map(r -> r.getDestination().getName()).collect(Collectors.toList());

        assertTrue(tagSet.contains("Trilogy System View"));

        assertThat(relationships, hasSize(3));
        assertTrue(relationshipNames.contains("GitHub"));
        assertTrue(relationshipNames.contains("XO Chat"));
        assertTrue(relationshipNames.contains("Trilogy Google G Suite"));
        assertEquals(person.getDescription(), "Product Chief Architect");
    }

    @Test
    public void should_build_system_xo_chat() throws Exception {
        String systemName = "XO Chat";
        Workspace workspace = getWorkspace();

        SoftwareSystem system = workspace.getModel().getSoftwareSystemWithName(systemName);
        Set<String> tagSet = system.getTagsAsSet();
        Set<Relationship> relationships = system.getRelationships();

        assertTrue(tagSet.contains("Trilogy System View"));

        assertThat(relationships, hasSize(0));
        assertThat(system.getContainers(), hasSize(0));

        assertEquals(system.getDescription(), "Realtime team communication");
    }

    @Test
    public void should_build_system_salesforce() throws Exception {
        String systemName = "SalesForce";
        Workspace workspace = getWorkspace();

        SoftwareSystem system = workspace.getModel().getSoftwareSystemWithName(systemName);
        Set<Relationship> relationships = system.getRelationships();

        assertThat(relationships, hasSize(1));
        assertThat(system.getContainers(), hasSize(0));

        assertEquals(system.getDescription(), "Book keeping");
        assertEquals(system.getLocation(), Location.External);
    }

    @Test
    public void should_build_system_trilogy_g_suite() throws Exception {
        String systemName = "Trilogy Google G Suite";
        Workspace workspace = getWorkspace();

        SoftwareSystem system = workspace.getModel().getSoftwareSystemWithName(systemName);
        Set<String> tagSet = system.getTagsAsSet();
        Set<Relationship> relationships = system.getRelationships();
        List<String> relationshipNames = relationships.stream().map(r -> r.getDestination().getName()).collect(Collectors.toList());

        assertTrue(tagSet.contains("Trilogy System View"));

        assertThat(relationships, hasSize(1));
        assertTrue(relationshipNames.contains("DevSpaces/DevSpaces API/Sign In Controller"));

        assertThat(system.getContainers(), hasSize(0));

        assertEquals(system.getDescription(), "Team collaboration via sheets, docs and presentations");
    }

    @Test
    public void should_build_system_github() throws Exception {
        String systemName = "GitHub";
        Workspace workspace = getWorkspace();

        SoftwareSystem system = workspace.getModel().getSoftwareSystemWithName(systemName);
        Set<String> tagSet = system.getTagsAsSet();
        Set<Relationship> relationships = system.getRelationships();

        assertTrue(tagSet.contains("DevSpaces System View"));
        assertTrue(tagSet.contains("DevSpaces Container View"));

        assertThat(relationships, hasSize(0));
        assertThat(system.getContainers(), hasSize(0));

        assertEquals(system.getDescription(), "Hosts code and used for identity management");
    }

    @Test
    public void should_build_system_devspaces() throws Exception {
        String systemName = "DevSpaces";
        Workspace workspace = getWorkspace();

        SoftwareSystem system = workspace.getModel().getSoftwareSystemWithName(systemName);
        Set<String> tagSet = system.getTagsAsSet();
        Set<Relationship> relationships = system.getRelationships();
        Set<Container> containers = system.getContainers();
        List<String> containerNames = system.getContainers().stream().map(c -> c.getName()).collect(Collectors.toList());

        assertTrue(tagSet.contains("DevSpaces System View"));

        assertThat(relationships, hasSize(0));

        assertThat(containers, hasSize(4));
        assertTrue(containerNames.contains("DevSpaces/DevSpaces CLI"));
        assertTrue(containerNames.contains("DevSpaces/DevSpaces API"));
        assertTrue(containerNames.contains("DevSpaces/DevSpaces Backend"));
        assertTrue(containerNames.contains("DevSpaces/DevSpaces Web Application"));

        assertEquals(system.getDescription(), "allows developers to collaborate");
    }

    @Test
    public void should_build_container_devspaces_backend() throws Exception {
        String systemName = "DevSpaces";
        String containerName = "DevSpaces/DevSpaces Backend";
        Workspace workspace = getWorkspace();

        SoftwareSystem system = workspace.getModel().getSoftwareSystemWithName(systemName);
        Container container = system.getContainerWithName(containerName);
        Set<String> tagSet = container.getTagsAsSet();

        assertTrue(tagSet.contains("DevSpaces Container View"));

        Set<Relationship> relationships = container.getRelationships();
        List<String> relationshipNames = relationships.stream().map(r -> r.getDestination().getName()).collect(Collectors.toList());

        assertThat(relationships, hasSize(1));
        assertTrue(relationshipNames.contains("DevSpaces/DevSpaces API"));

        assertEquals(container.getDescription(), "Restful API providing capabilities for interacting with a DevSpace");
        assertEquals(container.getTechnology(), "Spring Boot");
    }

    @Test
    public void should_build_container_devspaces_web_app() throws Exception {
        String systemName = "DevSpaces";
        String containerName = "DevSpaces/DevSpaces Web Application";
        Workspace workspace = getWorkspace();

        SoftwareSystem system = workspace.getModel().getSoftwareSystemWithName(systemName);
        Container container = system.getContainerWithName(containerName);
        Set<String> tagSet = container.getTagsAsSet();

        Set<Relationship> relationships = container.getRelationships();
        List<String> relationshipNames = relationships.stream().map(r -> r.getDestination().getName()).collect(Collectors.toList());

        assertTrue(tagSet.contains("DevSpaces Container View"));

        assertThat(relationships, hasSize(1));
        assertTrue(relationshipNames.contains("DevSpaces/DevSpaces Backend"));

        assertEquals(container.getDescription(), "Manage dev spaces");
        assertEquals(container.getTechnology(), "Angular");
    }

    @Test
    public void should_build_container_devspaces_api() throws Exception {
        String systemName = "DevSpaces";
        String containerName = "DevSpaces/DevSpaces API";
        Workspace workspace = getWorkspace();

        SoftwareSystem system = workspace.getModel().getSoftwareSystemWithName(systemName);
        Container container = system.getContainerWithName(containerName);
        Set<String> tagSet = container.getTagsAsSet();
        Set<Component> components = container.getComponents();
        List<String> componentNames = components.stream().map(c -> c.getName()).collect(Collectors.toList());

        Set<Relationship> relationships = container.getRelationships();

        assertThat(relationships, hasSize(0));

        assertTrue(tagSet.contains("DevSpaces Container View"));

        assertThat(components, hasSize(5));
        assertTrue(componentNames.contains("DevSpaces/DevSpaces API/Sign In Controller"));
        assertTrue(componentNames.contains("DevSpaces/DevSpaces API/Security Component"));
        assertTrue(componentNames.contains("DevSpaces/DevSpaces API/Reset Password Controller"));
        assertTrue(componentNames.contains("DevSpaces/DevSpaces API/E-mail Component"));

        assertEquals(container.getDescription(), "API to programmatically create/manage dev spaces");
        assertEquals(container.getTechnology(), "Spring Boot");
    }

    @Test
    public void should_build_component_devspaces_api_sign_in_controller() throws Exception {
        String systemName = "DevSpaces";
        String containerName = "DevSpaces/DevSpaces API";
        String componentName = "DevSpaces/DevSpaces API/Sign In Controller";
        Workspace workspace = getWorkspace();

        SoftwareSystem system = workspace.getModel().getSoftwareSystemWithName(systemName);
        Container container = system.getContainerWithName(containerName);
        Component component = container.getComponentWithName(componentName);
        Set<String> tagSet = component.getTagsAsSet();

        Set<Relationship> relationships = component.getRelationships();
        List<String> relationshipNames = relationships.stream().map(r -> r.getDestination().getName()).collect(Collectors.toList());

        assertThat(relationships, hasSize(1));
        assertTrue(relationshipNames.contains("DevSpaces/DevSpaces API/Security Component"));

        assertTrue(tagSet.contains("DevSpaces API Component View"));

        assertEquals(component.getDescription(), "Allows users to sign in");
        assertEquals(component.getTechnology(), "Spring MVC Rest Controller");
    }

    @Test
    public void should_build_component_devspaces_api_security_component() throws Exception {
        String systemName = "DevSpaces";
        String containerName = "DevSpaces/DevSpaces API";
        String componentName = "DevSpaces/DevSpaces API/Security Component";
        Workspace workspace = getWorkspace();

        SoftwareSystem system = workspace.getModel().getSoftwareSystemWithName(systemName);
        Container container = system.getContainerWithName(containerName);
        Component component = container.getComponentWithName(componentName);
        Set<String> tagSet = component.getTagsAsSet();

        Set<Relationship> relationships = component.getRelationships();
        List<String> relationshipNames = relationships.stream().map(r -> r.getDestination().getName()).collect(Collectors.toList());
        assertTrue(relationshipNames.contains("InfoSec"));

        assertThat(relationships, hasSize(1));

        assertTrue(tagSet.contains("DevSpaces API Component View"));

        assertEquals(component.getDescription(), "Provides functionality related to signing in, changing passwords, permissions, etc.");
        assertEquals(component.getTechnology(), "Spring Bean");
    }

    @Test
    public void should_build_component_devspaces_api_reset_password_controller() throws Exception {
        String systemName = "DevSpaces";
        String containerName = "DevSpaces/DevSpaces API";
        String componentName = "DevSpaces/DevSpaces API/Reset Password Controller";
        Workspace workspace = getWorkspace();

        SoftwareSystem system = workspace.getModel().getSoftwareSystemWithName(systemName);
        Container container = system.getContainerWithName(containerName);
        Component component = container.getComponentWithName(componentName);
        Set<String> tagSet = component.getTagsAsSet();

        Set<Relationship> relationships = component.getRelationships();
        List<String> relationshipNames = relationships.stream().map(r -> r.getDestination().getName()).collect(Collectors.toList());

        assertThat(relationships, hasSize(2));
        assertTrue(relationshipNames.contains("DevSpaces/DevSpaces API/Security Component"));
        assertTrue(relationshipNames.contains("DevSpaces/DevSpaces API/E-mail Component"));

        assertTrue(tagSet.contains("DevSpaces API Component View"));

        assertEquals(component.getDescription(), "Allows users to reset their passwords");
        assertEquals(component.getTechnology(), "Spring MVC Rest Controller");
    }

    @Test
    public void should_build_component_devspaces_api_email_component() throws Exception {
        String systemName = "DevSpaces";
        String containerName = "DevSpaces/DevSpaces API";
        String componentName = "DevSpaces/DevSpaces API/E-mail Component";
        Workspace workspace = getWorkspace();

        SoftwareSystem system = workspace.getModel().getSoftwareSystemWithName(systemName);
        Container container = system.getContainerWithName(containerName);
        Component component = container.getComponentWithName(componentName);
        Set<String> tagSet = component.getTagsAsSet();

        Set<Relationship> relationships = component.getRelationships();

        assertThat(relationships, hasSize(0));

        assertTrue(tagSet.contains("DevSpaces API Component View"));

        assertEquals(component.getDescription(), "Sends emails to users");
        assertEquals(component.getTechnology(), "Spring MVC Rest Controller");
    }

    private Workspace getWorkspace() throws IOException {
        File documentationRoot = new File(getClass().getResource(TestHelper.ROOT_PATH_TO_TEST_PRODUCT_DOCUMENTATION).getPath());
        File manifestFile = new File(documentationRoot + File.separator + "product-architecture.yml");

        ArchitectureDataStructure dataStructure = new ArchitectureDataStructureReader(new FilesFacade()).load(manifestFile);
        ArchitectureDataStructureTransformer transformer = TransformerFactory.create(documentationRoot);
        return transformer.toWorkSpace(dataStructure);
    }
}
