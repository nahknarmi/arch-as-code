package net.nahknarmi.arch.integration;

import com.structurizr.Workspace;
import com.structurizr.model.*;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.ArchitectureDataStructureReader;
import net.nahknarmi.arch.transformation.ArchitectureDataStructureTransformer;
import net.nahknarmi.arch.transformation.TransformerFactory;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertTrue;
import static net.nahknarmi.arch.TestHelper.TEST_PRODUCT_DOCUMENTATION_ROOT_PATH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

public class ParsedYamlToModelIntegrationTest {

    @Test
    public void should_build_person_developer() throws FileNotFoundException {
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
        assertTrue(relationshipNames.contains("DevSpaces CLI"));
        assertTrue(relationshipNames.contains("Trilogy Google G Suite"));
        assertTrue(relationshipNames.contains("Sign In Controller"));

        assertEquals(person.getDescription(), "Developer building software");
        assertEquals(person.getLocation(), Location.Internal);
    }

    @Test
    public void should_build_person_saasops() throws FileNotFoundException {
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
        assertTrue(relationshipNames.contains("DevSpaces Web Application"));
        assertTrue(relationshipNames.contains("Trilogy Google G Suite"));
        assertEquals(person.getDescription(), "SaasOps operating system");
    }

    @Test
    public void should_build_person_pca() throws FileNotFoundException {
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
    public void should_build_system_xo_chat() throws FileNotFoundException {
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
    public void should_build_system_salesforce() throws FileNotFoundException {
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
    public void should_build_system_trilogy_g_suite() throws FileNotFoundException {
        String systemName = "Trilogy Google G Suite";
        Workspace workspace = getWorkspace();

        SoftwareSystem system = workspace.getModel().getSoftwareSystemWithName(systemName);
        Set<String> tagSet = system.getTagsAsSet();
        Set<Relationship> relationships = system.getRelationships();
        List<String> relationshipNames = relationships.stream().map(r -> r.getDestination().getName()).collect(Collectors.toList());

        assertTrue(tagSet.contains("Trilogy System View"));

        assertThat(relationships, hasSize(1));
        assertTrue(relationshipNames.contains("Sign In Controller"));

        assertThat(system.getContainers(), hasSize(0));

        assertEquals(system.getDescription(), "Team collaboration via sheets, docs and presentations");
    }

    @Test
    public void should_build_system_github() throws FileNotFoundException {
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
    public void should_build_system_devspaces() throws FileNotFoundException {
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
        assertTrue(containerNames.contains("DevSpaces CLI"));
        assertTrue(containerNames.contains("DevSpaces API"));
        assertTrue(containerNames.contains("DevSpaces Backend"));
        assertTrue(containerNames.contains("DevSpaces Web Application"));

        assertEquals(system.getDescription(), "allows developers to collaborate");
    }

    @Test
    public void should_build_container_devspaces_backend() throws FileNotFoundException {
        String systemName = "DevSpaces";
        String containerName = "DevSpaces Backend";
        Workspace workspace = getWorkspace();

        SoftwareSystem system = workspace.getModel().getSoftwareSystemWithName(systemName);
        Container container = system.getContainerWithName(containerName);
        Set<String> tagSet = container.getTagsAsSet();

        assertTrue(tagSet.contains("DevSpaces Container View"));

        Set<Relationship> relationships = container.getRelationships();
        List<String> relationshipNames = relationships.stream().map(r -> r.getDestination().getName()).collect(Collectors.toList());

        assertThat(relationships, hasSize(1));
        assertTrue(relationshipNames.contains("DevSpaces API"));

        assertEquals(container.getDescription(), "Restful API providing capabilities for interacting with a DevSpace");
        assertEquals(container.getTechnology(), "Spring Boot");
    }

    @Test
    public void should_build_container_devspaces_web_app() throws FileNotFoundException {
        String systemName = "DevSpaces";
        String containerName = "DevSpaces Web Application";
        Workspace workspace = getWorkspace();

        SoftwareSystem system = workspace.getModel().getSoftwareSystemWithName(systemName);
        Container container = system.getContainerWithName(containerName);
        Set<String> tagSet = container.getTagsAsSet();

        Set<Relationship> relationships = container.getRelationships();
        List<String> relationshipNames = relationships.stream().map(r -> r.getDestination().getName()).collect(Collectors.toList());

        assertTrue(tagSet.contains("DevSpaces Container View"));

        assertThat(relationships, hasSize(1));
        assertTrue(relationshipNames.contains("DevSpaces Backend"));

        assertEquals(container.getDescription(), "Manage dev spaces");
        assertEquals(container.getTechnology(), "Angular");
    }

    @Test
    public void should_build_container_devspaces_api() throws FileNotFoundException {
        String systemName = "DevSpaces";
        String containerName = "DevSpaces API";
        Workspace workspace = getWorkspace();

        SoftwareSystem system = workspace.getModel().getSoftwareSystemWithName(systemName);
        Container container = system.getContainerWithName(containerName);
        Set<String> tagSet = container.getTagsAsSet();
        Set<Component> components = container.getComponents();
        List<String> componentNames = components.stream().map(c -> c.getName()).collect(Collectors.toList());

        Set<Relationship> relationships = container.getRelationships();

        assertThat(relationships, hasSize(0));

        assertTrue(tagSet.contains("DevSpaces Container View"));

        assertThat(components, hasSize(4));
        assertTrue(componentNames.contains("Sign In Controller"));
        assertTrue(componentNames.contains("Security Component"));
        assertTrue(componentNames.contains("Reset Password Controller"));
        assertTrue(componentNames.contains("E-mail Component"));

        assertEquals(container.getDescription(), "API to programmatically create/manage dev spaces");
        assertEquals(container.getTechnology(), "Spring Boot");
    }

    @Test
    public void should_build_component_devspaces_api_sign_in_controller() throws FileNotFoundException {
        String systemName = "DevSpaces";
        String containerName = "DevSpaces API";
        String componentName = "Sign In Controller";
        Workspace workspace = getWorkspace();

        SoftwareSystem system = workspace.getModel().getSoftwareSystemWithName(systemName);
        Container container = system.getContainerWithName(containerName);
        Component component = container.getComponentWithName(componentName);
        Set<String> tagSet = component.getTagsAsSet();

        Set<Relationship> relationships = component.getRelationships();
        List<String> relationshipNames = relationships.stream().map(r -> r.getDestination().getName()).collect(Collectors.toList());

        assertThat(relationships, hasSize(1));
        assertTrue(relationshipNames.contains("Security Component"));

        assertTrue(tagSet.contains("DevSpaces API Component View"));

        assertEquals(component.getDescription(), "Allows users to sign in");
        assertEquals(component.getTechnology(), "Spring MVC Rest Controller");
    }

    @Test
    public void should_build_component_devspaces_api_security_component() throws FileNotFoundException {
        String systemName = "DevSpaces";
        String containerName = "DevSpaces API";
        String componentName = "Security Component";
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
    public void should_build_component_devspaces_api_reset_password_controller() throws FileNotFoundException {
        String systemName = "DevSpaces";
        String containerName = "DevSpaces API";
        String componentName = "Reset Password Controller";
        Workspace workspace = getWorkspace();

        SoftwareSystem system = workspace.getModel().getSoftwareSystemWithName(systemName);
        Container container = system.getContainerWithName(containerName);
        Component component = container.getComponentWithName(componentName);
        Set<String> tagSet = component.getTagsAsSet();

        Set<Relationship> relationships = component.getRelationships();
        List<String> relationshipNames = relationships.stream().map(r -> r.getDestination().getName()).collect(Collectors.toList());

        assertThat(relationships, hasSize(2));
        assertTrue(relationshipNames.contains("Security Component"));
        assertTrue(relationshipNames.contains("E-mail Component"));

        assertTrue(tagSet.contains("DevSpaces API Component View"));

        assertEquals(component.getDescription(), "Allows users to reset their passwords");
        assertEquals(component.getTechnology(), "Spring MVC Rest Controller");
    }

    @Test
    public void should_build_component_devspaces_api_email_component() throws FileNotFoundException {
        String systemName = "DevSpaces";
        String containerName = "DevSpaces API";
        String componentName = "E-mail Component";
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

    private Workspace getWorkspace() throws FileNotFoundException {
        File documentationRoot = new File(getClass().getResource(TEST_PRODUCT_DOCUMENTATION_ROOT_PATH).getPath());
        File manifestFile = new File(documentationRoot + File.separator + "data-structure.yml");

        ArchitectureDataStructure dataStructure = new ArchitectureDataStructureReader().load(manifestFile);
        ArchitectureDataStructureTransformer transformer = TransformerFactory.create(documentationRoot);
        return transformer.toWorkSpace(dataStructure);
    }
}
