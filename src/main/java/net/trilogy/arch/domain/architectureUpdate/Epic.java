package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;

@Getter
@EqualsAndHashCode
public class Epic {
    public static final String BLANK_AU_EPIC_TITLE_VALUE = "Please enter epic title from Jira";
    public static final String BLANK_AU_EPIC_JIRA_LINK_VALUE = "Please enter epic link from Jira";
    public static final String BLANK_AU_EPIC_JIRA_TICKET_VALUE = "please-enter-epic-ticket-from-jira";

    @JsonProperty(value = "title")
    private final String title;
    @JsonProperty(value = "jira")
    private final Jira jira;

    @Builder(toBuilder = true)
    @JsonCreator(mode = PROPERTIES)
    public Epic(
            @JsonProperty("title") String title,
            @JsonProperty("jira") Jira jira
    ) {
        this.title = title;
        this.jira = jira;
    }

    public static Epic blank() {
        return new Epic(BLANK_AU_EPIC_TITLE_VALUE, new Jira(BLANK_AU_EPIC_JIRA_TICKET_VALUE, BLANK_AU_EPIC_JIRA_LINK_VALUE));
    }
}
