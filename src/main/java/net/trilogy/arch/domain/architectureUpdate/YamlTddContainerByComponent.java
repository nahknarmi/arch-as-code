package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddComponentReference;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddId;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;
import static java.util.stream.Collectors.toMap;

@ToString
@EqualsAndHashCode
public class YamlTddContainerByComponent {
    @Getter
    @JsonProperty(value = "component-id")
    private final TddComponentReference componentId;

    @Getter
    @JsonProperty(value = "component-path")
    private final String componentPath;

    @Getter
    @JsonProperty(value = "tdds")
    private final Map<TddId, YamlTdd> tdds;

    @JsonProperty(value = "deleted")
    private final Boolean deleted;

    @Builder(toBuilder = true)
    @JsonCreator(mode = PROPERTIES)
    public YamlTddContainerByComponent(
            @JsonProperty(value = "component-id") TddComponentReference componentId,
            @JsonProperty(value = "component-path") String componentPath,
            @JsonProperty(value = "deleted") Boolean deleted,
            @JsonProperty(value = "tdds") Map<TddId, YamlTdd> tdds
    ) {
        this.componentId = componentId;
        this.componentPath = componentPath;
        this.deleted = deleted;
        this.tdds = tdds;
    }

    public static YamlTddContainerByComponent blank() {
        return new YamlTddContainerByComponent(TddComponentReference.blank(), "c4://DevSpaces/DevSpaces-DevSpaces API/DevSpaces-DevSpaces API-E-mail Component Duplicate", false, Map.of(TddId.blank(), YamlTdd.blank()));
    }

    static TddContent contentByMatchingIds(List<TddContent> tddContents, YamlTddContainerByComponent componentTdds, TddId tddId) {
        return tddContents.stream()
                .filter(content -> content.getTdd().equals(tddId.toString()))
                .filter(content -> componentTdds.getComponentId() != null && content.getComponentId().equals(componentTdds.getComponentId().getId()))
                .findFirst()
                .orElse(null);
    }

    public YamlTddContainerByComponent updateTddContents(List<TddContent> tddContents) {
        final var tdds = getTdds().entrySet().stream()
                .map(it -> updateTddWithContent(it, tddContents))
                .collect(toMap(Entry::getKey, Entry::getValue));

        return toBuilder()
                .tdds(tdds)
                .build();
    }

    public boolean isDeleted() {
        return deleted != null && deleted;
    }

    private Entry<TddId, YamlTdd> updateTddWithContent(
            Entry<TddId, YamlTdd> it,
            List<TddContent> tddContents) {
        final var tddId = it.getKey();
        final var tdd = it.getValue().withContent(contentByMatchingIds(tddContents, this, tddId));

        return new SimpleEntry<>(tddId, tdd);
    }
}
