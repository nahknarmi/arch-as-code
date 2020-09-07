package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JiraJsonTest {
    // Do not configure for YAML: round-tripping as JSON is enough. We're not
    // testing the Jackson library on JSON vs YAML serialization
    public static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void round_trips_to_and_from_yaml() throws JsonProcessingException {
        final var jira = new Jira("some ticket", "some link");

        assertEquals(
                jira,
                mapper.readValue(mapper.writeValueAsString(jira), Jira.class)
        );
    }
}
