package net.trilogy.arch.domain.c4;

import com.structurizr.Workspace;
import com.structurizr.model.Component;
import com.structurizr.model.Container;
import com.structurizr.model.Person;
import com.structurizr.model.SoftwareSystem;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class C4PathTest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();

    private Workspace workspace;

    @Before
    public void setUp() {
        workspace = new Workspace("foo", "blah");
    }

    @Test
    public void shouldBuildPathForSystem() {
        SoftwareSystem element = buildSoftwareSystem("system");

        C4Path path = C4Path.buildPath(element);

        collector.checkThat(path.type(), equalTo(C4Type.SYSTEM));
        collector.checkThat(path.name(), equalTo("system"));
        collector.checkThat(path.getPath(), equalTo("c4://system"));
    }

    @Test
    public void shouldBuildPathForPerson() {
        Person element = buildPerson("person");

        C4Path path = C4Path.buildPath(element);

        collector.checkThat(path.type(), equalTo(C4Type.PERSON));
        collector.checkThat(path.name(), equalTo("person"));
        collector.checkThat(path.getPath(), equalTo("@person"));
    }

    @Test
    public void shouldBuildPathForContainer() {
        Container container = buildContainer("container");

        C4Path path = C4Path.buildPath(container);

        collector.checkThat(path.type(), equalTo(C4Type.CONTAINER));
        collector.checkThat(path.name(), equalTo("container"));
        collector.checkThat(path.getPath(), equalTo("c4://system/container"));
    }

    @Test
    public void shouldBuildPathForComponent() {
        Component component = buildComponent("component");

        C4Path path = C4Path.buildPath(component);

        collector.checkThat(path.type(), equalTo(C4Type.COMPONENT));
        collector.checkThat(path.name(), equalTo("component"));
        collector.checkThat(path.getPath(), equalTo("c4://system/container/component"));
    }

    @Test
    public void shouldBuildPathforEntitiesWithSlash() {
        C4Path personPath = C4Path.buildPath(buildPerson("person/1"));

        SoftwareSystem system = buildSoftwareSystem("system/1");
        C4Path systemPath = C4Path.buildPath(system);

        Container container = buildContainer("container/2/system/1", system);
        C4Path containerPath = C4Path.buildPath(container);

        C4Path componentPath = C4Path.buildPath(buildComponent("component/3/container/2/system/1", container));

        collector.checkThat(personPath.type(), equalTo(C4Type.PERSON));
        collector.checkThat(personPath.name(), equalTo("person/1"));
        collector.checkThat(personPath.getPath(), equalTo("@person\\/1"));

        collector.checkThat(systemPath.type(), equalTo(C4Type.SYSTEM));
        collector.checkThat(systemPath.name(), equalTo("system/1"));
        collector.checkThat(systemPath.getPath(), equalTo("c4://system\\/1"));

        collector.checkThat(containerPath.type(), equalTo(C4Type.CONTAINER));
        collector.checkThat(containerPath.name(), equalTo("container/2/system/1"));
        collector.checkThat(containerPath.getPath(), equalTo("c4://system\\/1/container\\/2\\/system\\/1"));

        collector.checkThat(componentPath.type(), equalTo(C4Type.COMPONENT));
        collector.checkThat(componentPath.name(), equalTo("component/3/container/2/system/1"));
        collector.checkThat(componentPath.getPath(), equalTo("c4://system\\/1/container\\/2\\/system\\/1/component\\/3\\/container\\/2\\/system\\/1"));
    }

    @Test
    public void shouldBuildPathforEntitiesWithDot() {
        C4Path personPath = C4Path.buildPath(buildPerson("person.1"));
        C4Path systemPath = C4Path.buildPath(buildSoftwareSystem("system.1"));
        C4Path containerPath = C4Path.buildPath(buildContainer("container.1"));
        C4Path componentPath = C4Path.buildPath(buildComponent("component.1"));

        collector.checkThat(personPath.type(), equalTo(C4Type.PERSON));
        collector.checkThat(personPath.name(), equalTo("person.1"));
        collector.checkThat(personPath.getPath(), equalTo("@person.1"));

        collector.checkThat(systemPath.type(), equalTo(C4Type.SYSTEM));
        collector.checkThat(systemPath.name(), equalTo("system.1"));
        collector.checkThat(systemPath.getPath(), equalTo("c4://system.1"));

        collector.checkThat(containerPath.type(), equalTo(C4Type.CONTAINER));
        collector.checkThat(containerPath.name(), equalTo("container.1"));
        collector.checkThat(containerPath.getPath(), equalTo("c4://system/container.1"));

        collector.checkThat(componentPath.type(), equalTo(C4Type.COMPONENT));
        collector.checkThat(componentPath.name(), equalTo("component.1"));
        collector.checkThat(componentPath.getPath(), equalTo("c4://system/container/component.1"));
    }

    @Test
    public void shouldBuildPathforEntitiesWithSpaces() {
        C4Path personPath = C4Path.buildPath(buildPerson("person 1"));
        C4Path systemPath = C4Path.buildPath(buildSoftwareSystem("system 1"));
        C4Path containerPath = C4Path.buildPath(buildContainer("container 1"));
        C4Path componentPath = C4Path.buildPath(buildComponent("component 1"));

        collector.checkThat(personPath.type(), equalTo(C4Type.PERSON));
        collector.checkThat(personPath.name(), equalTo("person 1"));
        collector.checkThat(personPath.getPath(), equalTo("@person 1"));

        collector.checkThat(systemPath.type(), equalTo(C4Type.SYSTEM));
        collector.checkThat(systemPath.name(), equalTo("system 1"));
        collector.checkThat(systemPath.getPath(), equalTo("c4://system 1"));

        collector.checkThat(containerPath.type(), equalTo(C4Type.CONTAINER));
        collector.checkThat(containerPath.name(), equalTo("container 1"));
        collector.checkThat(containerPath.getPath(), equalTo("c4://system/container 1"));

        collector.checkThat(componentPath.type(), equalTo(C4Type.COMPONENT));
        collector.checkThat(componentPath.name(), equalTo("component 1"));
        collector.checkThat(componentPath.getPath(), equalTo("c4://system/container/component 1"));
    }

    @Test
    public void shouldParsePathForPerson() {
        C4Path path = C4Path.path("@person");

        collector.checkThat(path.name(), equalTo("person"));
        collector.checkThat(path.personName(), equalTo("person"));
        collector.checkThat(path.type(), equalTo(C4Type.PERSON));
        collector.checkThat(path.getPath(), equalTo("@person"));
    }

    @Test
    public void shouldParsePathForSystem() {
        C4Path path = C4Path.path("c4://System");

        collector.checkThat(path.name(), equalTo("System"));
        collector.checkThat(path.systemName(), equalTo("System"));
        collector.checkThat(path.type(), equalTo(C4Type.SYSTEM));
        collector.checkThat(path.getPath(), equalTo("c4://System"));
    }

    @Test
    public void shouldParsePathForContainer() {
        C4Path path = C4Path.path("c4://DevSpaces/DevSpaces API");

        collector.checkThat(path.name(), equalTo("DevSpaces API"));
        collector.checkThat(path.systemName(), equalTo("DevSpaces"));
        collector.checkThat(path.containerName(), equalTo(Optional.of("DevSpaces API")));
        collector.checkThat(path.type(), equalTo(C4Type.CONTAINER));
        collector.checkThat(path.getPath(), equalTo("c4://DevSpaces/DevSpaces API"));
    }

    @Test
    public void shouldParsePathForComponent() {
        C4Path path = C4Path.path("c4://DevSpaces/DevSpaces API/Sign-In Component");

        collector.checkThat(path.name(), equalTo("Sign-In Component"));
        collector.checkThat(path.systemName(), equalTo("DevSpaces"));
        collector.checkThat(path.containerName(), equalTo(Optional.of("DevSpaces API")));
        collector.checkThat(path.componentName(), equalTo(Optional.of("Sign-In Component")));
        collector.checkThat(path.type(), equalTo(C4Type.COMPONENT));
        collector.checkThat(path.getPath(), equalTo("c4://DevSpaces/DevSpaces API/Sign-In Component"));
    }

    @Test
    public void shouldParseEntitiesWithSlashInPath() {
        C4Path personPath = C4Path.path("@person\\/1");
        C4Path systemPath = C4Path.path("c4://system\\/1");
        C4Path containerPath = C4Path.path("c4://system\\/1/container\\/2\\/system\\/1");
        C4Path componentPath = C4Path.path("c4://system\\/1/container\\/2\\/system\\/1/component\\/3\\/container\\/2\\/system\\/1");

        collector.checkThat(personPath.name(), equalTo("person/1"));
        collector.checkThat(personPath.type(), equalTo(C4Type.PERSON));
        collector.checkThat(personPath.personName(), equalTo("person/1"));

        collector.checkThat(systemPath.name(), equalTo("system/1"));
        collector.checkThat(systemPath.type(), equalTo(C4Type.SYSTEM));
        collector.checkThat(systemPath.systemName(), equalTo("system/1"));

        collector.checkThat(containerPath.name(), equalTo("container/2/system/1"));
        collector.checkThat(containerPath.type(), equalTo(C4Type.CONTAINER));
        collector.checkThat(containerPath.containerName(), equalTo(Optional.of("container/2/system/1")));

        collector.checkThat(componentPath.name(), equalTo("component/3/container/2/system/1"));
        collector.checkThat(componentPath.type(), equalTo(C4Type.COMPONENT));
        collector.checkThat(componentPath.componentName(), equalTo(Optional.of("component/3/container/2/system/1")));
    }

    @Test
    public void shouldBeAbleToExtractSubPathsInsystemPath() {
        C4Path path = C4Path.path("c4://sys1");
        collector.checkThat(path.systemPath(), equalTo(path));
    }

    @Test
    public void shouldBeAbleToExtractSubPathsInpersonPath() {
        C4Path path = C4Path.path("@person");
        collector.checkThat(path.personPath(), equalTo(path));
    }

    @Test
    public void shouldBeAbleToExtractSubPathsInContainerPath() {
        C4Path path = C4Path.path("c4://system\\/1/container\\/2\\/system\\/1");

        collector.checkThat(path.systemPath(), equalTo(C4Path.path("c4://system\\/1")));
        collector.checkThat(path.containerPath(), equalTo(path));
    }

    @Test
    public void shouldBeAbleToExtractSubPathsInComponentPath() {
        C4Path path = C4Path.path("c4://system\\/1/container\\/2\\/system\\/1/component\\/3\\/container\\/2\\/system\\/1");
        collector.checkThat(path.systemPath(), equalTo(C4Path.path("c4://system\\/1")));
        collector.checkThat(path.containerPath(), equalTo(C4Path.path("c4://system\\/1/container\\/2\\/system\\/1")));
        collector.checkThat(path.componentPath(), equalTo(path));
    }

    @Test
    public void shouldBuildPathFromValidPaths() {
        collector.checkThat(C4Path.path("@Person"), notNullValue());
        collector.checkThat(C4Path.path("c4://system1"), notNullValue());
        collector.checkThat(C4Path.path("c4://system1/container1"), notNullValue());
        collector.checkThat(C4Path.path("c4://system1/container1/component1"), notNullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToBuildPathIfPrefixIsInvalid() {
        collector.checkThat(C4Path.path("{@Person"), notNullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingPersonThrowsException() {
        C4Path.path("@");
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildingPersonPathWithOnlySlashThrowsException() {
        C4Path path = C4Path.path("@\\/");
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildingSystemPathWithOnlySlashThrowsException() {
        C4Path path = C4Path.path("c4://\\/");
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingSystemThrowsException() {
        C4Path.path("c4://");
    }

    @Test(expected = IllegalStateException.class)
    public void accessingSystemPathOnNonSystemThrowsException() {
        C4Path path = C4Path.path("@person");
        path.systemPath();
    }

    @Test(expected = IllegalStateException.class)
    public void accessingContainerPathOnNonContainerThrowsException() {
        C4Path path = C4Path.path("@person");
        path.containerPath();
    }

    @Test(expected = IllegalStateException.class)
    public void accessingComponentPathOnNonComponentThrowsException() {
        C4Path path = C4Path.path("@person");
        path.componentPath();
    }

    @Test(expected = IllegalStateException.class)
    public void accessingPersonPathOnNonPersonThrowsException() {
        C4Path path = C4Path.path("c4://sys1");
        path.personPath();
    }

    @Test(expected = IllegalStateException.class)
    public void accessingComponentPathOnPathWithNoComponentThrowsException() {
        C4Path path = C4Path.path("c4://sys1/container1");
        path.componentPath();
    }

    private Component buildComponent(String componentName) {
        return buildComponent(componentName, null);
    }

    private Component buildComponent(String componentName, Container container) {
        if (container == null) container = buildContainer("container");

        return container.addComponent(componentName, "bar");
    }

    private Container buildContainer(String containerName) {
        return buildContainer(containerName, null);
    }

    private Container buildContainer(String containerName, SoftwareSystem softwareSystem) {

        if (softwareSystem == null) {
            softwareSystem = workspace.getModel().getSoftwareSystemWithName("system");

            if (softwareSystem == null) {
                softwareSystem = buildSoftwareSystem("system");
            }
        }

        return softwareSystem.addContainer(containerName, "bar", "bazz");
    }

    private SoftwareSystem buildSoftwareSystem(String systemName) {
        return workspace.getModel().addSoftwareSystem(systemName, "bar");
    }

    private Person buildPerson(String personName) {
        return workspace.getModel().addPerson(personName, "bar");
    }
}
