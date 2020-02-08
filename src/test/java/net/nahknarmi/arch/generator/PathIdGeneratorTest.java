package net.nahknarmi.arch.generator;

import com.structurizr.Workspace;
import com.structurizr.model.*;
import net.nahknarmi.arch.domain.c4.*;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.collect.ImmutableSet.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class PathIdGeneratorTest {
    private final String SYSTEM_PATH = "c4://system_1";
    private final String SYSTEM_NAME = "core banking";
    private final String PERSON_PATH = "@jsmith";
    private final String PERSON_NAME = "John Smith";
    private final String CONTAINER_NAME = "WebLogic";
    private final String CONTAINER_PATH = SYSTEM_PATH + "/container_2";
    private final String COMPONENT_NAME = "collections";
    private final String COMPONENT_PATH = CONTAINER_PATH + "/collections_3";

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
                model.addSoftwareSystem(SYSTEM_NAME, "desc")
                        .addContainer(CONTAINER_NAME, "desc", "J2EE")
                        .addComponent(COMPONENT_NAME, "desc");

        assertThat(component.getId(), equalTo(COMPONENT_PATH));
    }

    private C4Model buildC4Model() {
        return C4Model.builder()
                .systems(
                        of(
                                C4SoftwareSystem.builder()
                                        .name(SYSTEM_NAME)
                                        .path(new C4Path(SYSTEM_PATH))
                                        .description("irrelevant")
                                        .build()
                        )
                )

                .people(
                        of(
                                C4Person.builder()
                                        .name(PERSON_NAME)
                                        .path(new C4Path(PERSON_PATH))
                                        .description("irrelevant")
                                        .build()
                        )
                )

                .containers(
                        of(
                                C4Container.builder()
                                        .name(CONTAINER_NAME)
                                        .path(new C4Path(CONTAINER_PATH))
                                        .description("irrelevant")
                                        .build())
                )

                .components(
                        of(
                                C4Component.builder()
                                        .name(COMPONENT_NAME)
                                        .path(new C4Path(COMPONENT_PATH))
                                        .description("irrelevant")
                                        .build()

                        )
                )
                .build();
    }

    private Model buildModel(IdGenerator idGenerator) {
        Model model = new Workspace("foo", "bar").getModel();
        model.setIdGenerator(idGenerator);
        return model;
    }
}