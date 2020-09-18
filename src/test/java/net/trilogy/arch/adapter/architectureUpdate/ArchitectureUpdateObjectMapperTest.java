package net.trilogy.arch.adapter.architectureUpdate;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import org.junit.Test;

import static net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper.YAML_OBJECT_MAPPER;
import static net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate.blank;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ArchitectureUpdateObjectMapperTest {
    private static String getBlankYamlText() {
        return String.join("\n"
                , ""
                , "name: '[SAMPLE NAME]'"
                , "milestone: '[SAMPLE MILESTONE]'"
                , "authors:"
                , "- name: '[SAMPLE PERSON NAME]'"
                , "  email: '[SAMPLE PERSON EMAIL]'"
                , "PCAs:"
                , "- name: '[SAMPLE PERSON NAME]'"
                , "  email: '[SAMPLE PERSON EMAIL]'"
                , "P2:"
                , "  link: '[SAMPLE LINK TO P2]'"
                , "  jira:"
                , "    ticket: '[SAMPLE JIRA TICKET]'"
                , "    link: '[SAMPLE JIRA TICKET LINK]'"
                , "P1:"
                , "  link: '[SAMPLE LINK TO P1]'"
                , "  jira:"
                , "    ticket: '[SAMPLE JIRA TICKET]'"
                , "    link: '[SAMPLE JIRA TICKET LINK]'"
                , "  executive-summary: '[SAMPLE EXECUTIVE SUMMARY]'"
                , "useful-links:"
                , "- description: '[SAMPLE LINK DESCRIPTION]'"
                , "  link: '[SAMPLE-LINK]'"
                , "milestone-dependencies:"
                , "- description: '[SAMPLE MILESTONE DEPENDENCY]'"
                , "  links:"
                , "  - description: '[SAMPLE LINK DESCRIPTION]'"
                , "    link: '[SAMPLE-LINK]'"
                , "decisions:"
                , "  '[SAMPLE-DECISION-ID]':"
                , "    text: '[SAMPLE DECISION TEXT]'"
                , "    tdd-references:"
                , "    - '[SAMPLE-TDD-ID]'"
                , "tdds-per-component:"
                , "- component-id: '[SAMPLE-COMPONENT-ID]'"
                , "  deleted: false"
                , "  tdds:"
                , "    '[SAMPLE-TDD-ID]':"
                , "      text: |-"
                , "        [SAMPLE TDD TEXT LONG TEXT FORMAT]"
                , "        Line 2"
                , "        Line 3"
                , "functional-requirements:"
                , "  '[SAMPLE-REQUIREMENT-ID]':"
                , "    text: '[SAMPLE REQUIREMENT TEXT]'"
                , "    source: '[SAMPLE REQUIREMENT SOURCE TEXT]'"
                , "    tdd-references:"
                , "    - '[SAMPLE-TDD-ID]'"
                , "capabilities:"
                , "  epic:"
                , "    title: Please enter epic title from Jira"
                , "    jira:"
                , "      ticket: please-enter-epic-ticket-from-jira"
                , "      link: Please enter epic link from Jira"
                , "  feature-stories:"
                , "  - title: '[SAMPLE FEATURE STORY TITLE]'"
                , "    jira:"
                , "      ticket: \"\""
                , "      link: \"\""
                , "    tdd-references:"
                , "    - '[SAMPLE-TDD-ID]'"
                , "    functional-requirement-references:"
                , "    - '[SAMPLE-REQUIREMENT-ID]'"
                , "    e2e:"
                , "      title: E2E title"
                , "      business-goal: Need to do this"
                , "      functional-area-id: '[Sample Functional Area Id]'"
                , "      jira:"
                , "        ticket: '[SAMPLE JIRA TICKET]'"
                , "        link: '[SAMPLE JIRA TICKET LINK]'"
                , "      attributes:"
                , "      - name: Accessible"
                , "        rationale: UI need to be Accessible"
                , "        jira:"
                , "          ticket: '[SAMPLE JIRA TICKET]'"
                , "          link: '[SAMPLE JIRA TICKET LINK]'"
                , "functional-areas:"
                , "  '[Sample Functional Area Id]':"
                , "    title: Sample title"
                , "    jira:"
                , "      ticket: '[SAMPLE JIRA TICKET]'"
                , "      link: '[SAMPLE JIRA TICKET LINK]'"
        );
    }

    @Test
    public void shouldWriteBlank() throws Exception {
        final var actual = YAML_OBJECT_MAPPER.writeValueAsString(blank());
        final var expected = getBlankYamlText();

        assertThat(actual.trim(), equalTo(expected.trim()));
    }

    @Test
    public void shouldReadBlank() throws JsonProcessingException {
        final var actual = YAML_OBJECT_MAPPER.readValue(getBlankYamlText(), ArchitectureUpdate.class);

        assertThat(actual, equalTo(blank()));
    }

    @Test
    public void shouldWriteBlankYamlWithOverriddenName() throws Exception {
        final var actual = YAML_OBJECT_MAPPER.writeValueAsString(
                ArchitectureUpdate.prefilledWithBlanks()
                        .name("OVERRIDDEN")
                        .build());
        final var expected = getBlankYamlText().replace("'[SAMPLE NAME]'", "OVERRIDDEN");

        assertThat(actual.trim(), equalTo(expected.trim()));
    }
}
