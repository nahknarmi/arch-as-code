package net.nahknarmi.arch.domain;


import net.nahknarmi.arch.adapter.in.ArchitectureDataStructureReader;
import net.nahknarmi.arch.domain.c4.C4Model;
import net.nahknarmi.arch.domain.c4.C4Person;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;

import static net.nahknarmi.arch.TestHelper.TEST_SPACES_MANIFEST_PATH;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;


public class ArchitectureDataStructureReaderTest {
    private static final String PRODUCT_NAME = "TestSpaces";

    @Test
    public void should_load_architecture_data_structure_from_yaml_file() throws IOException {
        File productDocumentationRoot = new File(getClass().getResource(TEST_SPACES_MANIFEST_PATH).getPath());
        ArchitectureDataStructure dataStructure = new ArchitectureDataStructureReader().load(productDocumentationRoot);

        assertNotNull(dataStructure);
        assertThat(dataStructure.getName(), is(equalTo(PRODUCT_NAME)));
        assertThat(dataStructure.getBusinessUnit(), is(equalTo("DevFactory")));
        assertThat(dataStructure.getDescription(), is(equalTo("TestSpaces is a tool!")));


        assertThat(dataStructure.getDecisions().size(), is(equalTo(2)));
        assertThat(dataStructure.getDecisions().get(0).getTitle(), is(equalTo("Docker as the containerization technology platform")));
        assertThat(decisionDate(dataStructure.getDecisions().get(0)), is(equalTo(LocalDate.of(2018, 11, 29))));
        assertThat(dataStructure.getDecisions().get(1).getTitle(), is(equalTo("Kubernetes as the container management platform")));
        assertThat(decisionDate(dataStructure.getDecisions().get(1)), is(equalTo(LocalDate.of(2019, 11, 19))));


        //it should have model
        C4Model model = dataStructure.getModel();
        assertThat(model, notNullValue());

        //it should have people
        Set<C4Person> people = model.getPeople();

        assertThat(people.size(), equalTo(4));
        C4Person person = model.findPersonByName("Developer");
        assertThat(person, notNullValue());
        assertThat(person.getName(), is(equalTo("Developer")));
        assertThat(person.getDescription(), is(equalTo("Developer building software")));

        //it should have systems
        assertThat(model.getSystems().size(), is(equalTo(5)));
    }

    private LocalDate decisionDate(ImportantTechnicalDecision decision) {
        return Instant.ofEpochMilli(decision.getDate().getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
    }
}
