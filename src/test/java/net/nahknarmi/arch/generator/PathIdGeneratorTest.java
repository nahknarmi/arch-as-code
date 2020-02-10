package net.nahknarmi.arch.generator;

import com.google.common.collect.ImmutableList;
import com.structurizr.Workspace;
import com.structurizr.model.*;
import net.nahknarmi.arch.domain.c4.*;
import org.junit.Before;
import org.junit.Test;

import static net.nahknarmi.arch.domain.c4.C4Path.path;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class PathIdGeneratorTest {
    private final String SYSTEM_PATH = "c4://system_1";
    private final String SYSTEM_NAME = "core banking";
    private final String PERSON_PATH = "@jsmith";
    private final String PERSON_NAME = "John Smith";
    private final String CONTAINER_NAME = "WebLogic";
    private final String CONTAINER_NAME2 = "WebSphere";
    private final String CONTAINER_PATH = SYSTEM_PATH + "/container_1";
    private final String CONTAINER_PATH2 = SYSTEM_PATH + "/container_2";
    private final String COMPONENT_NAME = "collections";
    private final String COMPONENT_PATH = CONTAINER_PATH + "/collections_1";
    private final String COMPONENT_PATH2 = CONTAINER_PATH2 + "/collections_2";

    private IdGenerator idGenerator;

    @Before
    public void setUp() {
        this.idGenerator = new PathIdGenerator(buildC4Model());
    }

    @Test
    public void should_handle_system() {
        Model model = buildModel(idGenerator);

        SoftwareSystem softwareSystem = model.addSoftwareSystem(SYSTEM_NAME, "desc");

        assertThat(softwareSystem.getId(), equalTo(SYSTEM_PATH));
    }

    @Test
    public void should_handle_person() {
        Model model = buildModel(idGenerator);

        Person person = model.addPerson(PERSON_NAME, "desc");

        assertThat(person.getId(), equalTo(PERSON_PATH));
    }

    @Test
    public void should_handle_container() {
        Model model = buildModel(idGenerator);

        Container container =
                model.addSoftwareSystem(SYSTEM_NAME, "desc")
                        .addContainer(CONTAINER_NAME, "desc", "J2EE");

        assertThat(container.getId(), equalTo(CONTAINER_PATH));
    }

    @Test
    public void should_handle_component() {
        Model model = buildModel(idGenerator);

        Component component =
                model.addSoftwareSystem(SYSTEM_NAME, SYSTEM_PATH)
                        .addContainer(CONTAINER_NAME, CONTAINER_PATH, "J2EE")
                        .addComponent(COMPONENT_NAME, COMPONENT_PATH);

        assertThat(component.getId(), equalTo(COMPONENT_PATH));
    }

    @Test
    public void should_handle_component_with_same_name() {
        Model model = buildModel(idGenerator);

        Component component =
                model.addSoftwareSystem(SYSTEM_NAME, SYSTEM_PATH)
                        .addContainer(CONTAINER_NAME2, CONTAINER_PATH2, "J2EE")
                        .addComponent(COMPONENT_NAME, COMPONENT_PATH2);

        assertThat(component.getId(), equalTo(COMPONENT_PATH2));
    }

    @Test
    public void should_handle_relationship_id() {
        Model model = buildModel(idGenerator);

        Component component =
                model.addSoftwareSystem(SYSTEM_NAME, SYSTEM_PATH)
                        .addContainer(CONTAINER_NAME2, CONTAINER_PATH2, "J2EE")
                        .addComponent(COMPONENT_NAME, COMPONENT_PATH2);

        Person person = model.addPerson(PERSON_NAME, "fooo");

        person.uses(component, "foo");


        assertThat(component.getId(), equalTo(COMPONENT_PATH2));
    }

    private C4Model buildC4Model() {
        return new C4Model()
                .addSoftwareSystem(
                        C4SoftwareSystem.builder()
                                .name(SYSTEM_NAME)
                                .path(path(SYSTEM_PATH))
                                .description("irrelevant")
                                .relationships(ImmutableList.of(C4Relationship.builder()
                                        .action(C4Action.USES)
                                        .with(path(PERSON_PATH))
                                        .technology("SOAP")
                                        .description("Foo bar")
                                        .build()))
                                .build()
                ).addPerson(
                        C4Person.builder()
                                .name(PERSON_NAME)
                                .path(path(PERSON_PATH))
                                .description("irrelevant")
                                .build()
                ).addContainer(
                        C4Container.builder()
                                .name(CONTAINER_NAME)
                                .path(path(CONTAINER_PATH))
                                .description("irrelevant")
                                .build())
                .addContainer(
                        C4Container.builder()
                                .name(CONTAINER_NAME2)
                                .path(path(CONTAINER_PATH2))
                                .description("irrelevant")
                                .build()
                )
                .addComponent(
                        C4Component.builder()
                                .name(COMPONENT_NAME)
                                .path(path(COMPONENT_PATH))
                                .description("irrelevant")
                                .build()
                ).addComponent(
                        C4Component.builder()
                                .name(COMPONENT_NAME)
                                .path(path(COMPONENT_PATH2))
                                .description("irrelevant")
                                .build()
                );
    }

    private Model buildModel(IdGenerator idGenerator) {
        Model model = new Workspace("foo", "bar").getModel();
        model.setIdGenerator(idGenerator);
        return model;
    }
}
