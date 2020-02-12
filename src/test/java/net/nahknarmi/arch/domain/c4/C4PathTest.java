package net.nahknarmi.arch.domain.c4;

import com.structurizr.Workspace;
import com.structurizr.model.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static net.nahknarmi.arch.domain.c4.C4Path.path;
import static net.nahknarmi.arch.domain.c4.C4Type.component;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;


public class C4PathTest {
    private Workspace workspace;

    @Before
    public void setUp() {
        workspace = new Workspace("foo", "blah");
//        workspace.getModel().setIdGenerator(new PathIdGenerator());
    }

    @Test
    public void buildPath_for_system() {
        SoftwareSystem element = buildSoftwareSystem();
        buildPath(element, C4Type.system, "c4://system");
    }

    @Test
    public void buildPath_for_person() {
        Person element = workspace.getModel().addPerson("person", "bar");
        buildPath(element, C4Type.person, "@person");
    }

    @Test
    public void buildPath_for_container() {
        Container container = buildContainer();
        buildPath(container, C4Type.container, "c4://system/container");
    }

    @Test
    public void buildPath_for_component() {
        Component component = buildComponent("component");
        buildPath(component, C4Type.component, "c4://system/container/component");
    }

    @Test
    public void buildPath_for_component_with_dot() {
        Component component = buildComponent("Sococo.App");
        buildPath(component, C4Type.component, "c4://system/container/Sococo.App");
    }

    @Test
    public void buildPath_for_component_with_slash() {
        Component component = buildComponent("component/abc");
        buildPath(component, C4Type.component, "c4://system/container/component-abc");
    }

    private Component buildComponent(String name) {
        Container container = buildContainer();
        return container.addComponent(name, "bar");
    }

    private Container buildContainer() {
        SoftwareSystem softwareSystem = buildSoftwareSystem();
        return softwareSystem.addContainer("container", "bar", "bazz");
    }

    private SoftwareSystem buildSoftwareSystem() {
        return workspace.getModel().addSoftwareSystem("system", "bar");
    }

    private void buildPath(Element element, C4Type expectedType, String expectedPath) {
        C4Path path = C4Path.buildPath(element);
        assertThat(path.type(), equalTo(expectedType));
        assertThat(path.getPath(), equalTo(expectedPath));
    }

    @Test
    public void person() {
        C4Path path = path("@PCA");

        assertThat(path.name(), equalTo("PCA"));
        assertThat(path.type(), equalTo(C4Type.person));
    }

    @Test
    public void system() {
        C4Path path = path("c4://DevSpaces");

        assertThat(path.name(), equalTo("DevSpaces"));
        assertThat(path.systemName(), equalTo("DevSpaces"));
        assertThat(path.type(), equalTo(C4Type.system));
    }

    @Test
    public void container() {
        C4Path path = path("c4://DevSpaces/DevSpaces API");

        assertThat(path.name(), equalTo("DevSpaces API"));
        assertThat(path.systemName(), equalTo("DevSpaces"));
        assertThat(path.containerName(), equalTo(Optional.of("DevSpaces API")));

        assertThat(path.type(), equalTo(C4Type.container));
    }

    @Test
    public void component() {
        C4Path path = path("c4://DevSpaces/DevSpaces API/Sign-In Component");

        assertThat(path.name(), equalTo("Sign-In Component"));
        assertThat(path.systemName(), equalTo("DevSpaces"));
        assertThat(path.containerName(), equalTo(Optional.of("DevSpaces API")));
        assertThat(path.componentName(), equalTo(Optional.of("Sign-In Component")));

        assertThat(path.type(), equalTo(component));
    }

    @Test(expected = IllegalArgumentException.class)
    public void missing_person() {
        path("@");
    }

    @Test(expected = IllegalArgumentException.class)
    public void missing_system() {
        path("c4://");
    }


    @Test(expected = IllegalStateException.class)
    public void accessing_system_path_on_non_system_throws_exception() {
        C4Path path = path("@person");
        path.systemPath();
    }

    @Test(expected = IllegalStateException.class)
    public void accessing_person_path_on_non_person_throws_exception() {
        C4Path path = path("c4://sys1");
        path.personPath();
    }

    @Test(expected = IllegalStateException.class)
    public void accessing_component_path_on_path_with_no_component_throws_exception() {
        C4Path path = path("c4://sys1/container1");
        path.componentPath();
    }

    @Test
    public void system_path() {
        C4Path path = path("c4://sys_1");
        assertThat(path.systemPath(), equalTo(path));
    }

    @Test
    public void person_path() {
        C4Path path = path("@person");
        assertThat(path.personPath(), equalTo(path));
    }

    @Test
    public void should_be_able_to_extract_sub_paths_in_container_path() {
        C4Path path = path("c4://sys1/container1");
        assertThat(path.systemPath(), equalTo(path("c4://sys1")));
        assertThat(path.containerPath(), equalTo(path));
    }

    @Test
    public void should_be_able_to_extract_sub_paths_in_component_path() {
        C4Path path = path("c4://sys1/container1/comp1");
        assertThat(path.systemPath(), equalTo(path("c4://sys1")));
        assertThat(path.containerPath(), equalTo(path("c4://sys1/container1")));
        assertThat(path.componentPath(), equalTo(path));
    }

    @Test
    public void should_build_path_from_valid_paths() {
        assertThat(path("@Person"), notNullValue());
        assertThat(path("c4://system1"), notNullValue());
        assertThat(path("c4://system1/container1"), notNullValue());
        assertThat(path("c4://system1/container1/component1"), notNullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_fail_to_build_path_if_prefix_is_invalid() {
        assertThat(path("{@Person"), notNullValue());
    }
}
