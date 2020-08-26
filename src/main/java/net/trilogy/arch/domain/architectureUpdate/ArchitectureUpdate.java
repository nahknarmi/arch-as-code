package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@ToString
@EqualsAndHashCode
@JsonPropertyOrder(value = {
        "name",
        "milestone",
        "authors",
        "PCAs",
        "P2",
        "P1",
        "useful-links",
        "milestone-dependencies",
        "decisions",
        "tdds-per-component",
        "functional-requirements",
        "capabilities"
})
public class ArchitectureUpdate {
    @JsonProperty(value = "name") private final String name;
    @JsonProperty(value = "milestone") private final String milestone;
    @JsonProperty(value = "authors") private final List<Person> authors;
    @JsonProperty(value = "PCAs") private final List<Person> PCAs;
    @JsonProperty(value = "P2") private final P2 p2;
    @JsonProperty(value = "P1") private final P1 p1;
    @JsonProperty(value = "useful-links") private final List<Link> usefulLinks;
    @JsonProperty(value = "milestone-dependencies") private final List<MilestoneDependency> milestoneDependencies;
    @JsonProperty(value = "decisions") private final Map<Decision.Id, Decision> decisions;
    @JsonProperty(value = "tdds-per-component") private final List<TddContainerByComponent> tddContainersByComponent;
    @JsonProperty(value = "functional-requirements") private final Map<FunctionalRequirement.Id, FunctionalRequirement> functionalRequirements;
    @JsonProperty(value = "capabilities") private final CapabilitiesContainer capabilityContainer;

    //TODO remove tddContents per https://tw-trilogy.atlassian.net/browse/AAC-160
    @JsonIgnore private final List<TddContent> tddContents;

    @Builder(toBuilder = true)
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public ArchitectureUpdate(
            @JsonProperty("name") String name,
            @JsonProperty("milestone") String milestone,
            @JsonProperty("authors") List<Person> authors,
            @JsonProperty("PCAs") List<Person> PCAs,
            @JsonProperty("decisions") Map<Decision.Id, Decision> decisions,
            @JsonProperty("tdds-per-component") List<TddContainerByComponent> tddContainersByComponent,
            @JsonProperty("functional-requirements") Map<FunctionalRequirement.Id, FunctionalRequirement> functionalRequirements,
            @JsonProperty("capabilities") CapabilitiesContainer capabilityContainer,
            @JsonProperty("p2") P2 p2,
            @JsonProperty("p1") P1 p1,
            @JsonProperty("useful-links") List<Link> usefulLinks,
            @JsonProperty("milestone-dependencies") List<MilestoneDependency> milestoneDependencies,
            @JsonProperty("tddContents") List<TddContent> tddContents
    ) {
        this.name = name;
        this.milestone = milestone;
        this.authors = authors;
        this.PCAs = PCAs;
        this.decisions = decisions;
        this.tddContainersByComponent = tddContainersByComponent;
        this.functionalRequirements = functionalRequirements;
        this.capabilityContainer = capabilityContainer;
        this.p2 = p2;
        this.p1 = p1;
        this.usefulLinks = usefulLinks;
        this.milestoneDependencies = milestoneDependencies;
        this.tddContents = tddContents;
    }

    public static ArchitectureUpdateBuilder builderPreFilledWithBlanks() {
        return ArchitectureUpdate.builder()
                .name("[SAMPLE NAME]")
                .milestone("[SAMPLE MILESTONE]")
                .authors(List.of(Person.blank()))
                .PCAs(List.of(Person.blank()))
                .decisions(Map.of(Decision.Id.blank(), Decision.blank()))
                .tddContainersByComponent(List.of(TddContainerByComponent.blank()))
                .functionalRequirements(Map.of(FunctionalRequirement.Id.blank(), FunctionalRequirement.blank()))
                .capabilityContainer(CapabilitiesContainer.blank())
                .p2(P2.blank())
                .p1(P1.blank())
                .usefulLinks(List.of(Link.blank()))
                .milestoneDependencies(List.of(MilestoneDependency.blank()));
    }

    public static ArchitectureUpdate blank() {
        return builderPreFilledWithBlanks().build();
    }

    public ArchitectureUpdate addJiraToFeatureStory(FeatureStory storyToChange, Jira jiraToAdd) {
        return this.toBuilder().capabilityContainer(
                this.getCapabilityContainer().toBuilder()
                        .featureStories(
                                this.getCapabilityContainer().getFeatureStories().stream()
                                        .map(story -> {
                                            if (story.equals(storyToChange)) {
                                                return story.toBuilder().jira(jiraToAdd).build();
                                            }
                                            return story;
                                        })
                                        .collect(Collectors.toList())
                        ).build()
        ).build();
    }
}
