package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddComponentReference;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddId;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;

@ToString
@EqualsAndHashCode
public class TddContainerByComponent {
    @Getter
    @JsonProperty(value = "component-id")
    private final TddComponentReference componentId;

    @Getter
    @JsonProperty(value = "component-path")
    private final String componentPath;

    @Getter
    @JsonProperty(value = "tdds")
    private final Map<TddId, Tdd> tdds;

    @JsonProperty(value = "deleted")
    private final Boolean deleted;

    @Builder(toBuilder = true)
    @JsonCreator(mode = PROPERTIES)
    public TddContainerByComponent(
            @JsonProperty(value = "component-id") TddComponentReference componentId,
            @JsonProperty(value = "component-path") String componentPath,
            @JsonProperty(value = "deleted") Boolean deleted,
            @JsonProperty(value = "tdds") Map<TddId, Tdd> tdds
    ) {
        this.componentId = componentId;
        this.componentPath = componentPath;
        this.deleted = deleted;
        this.tdds = tdds;
    }

    public static TddContainerByComponent blank() {
        return new TddContainerByComponent(TddComponentReference.blank(), null, false, Map.of(TddId.blank(), Tdd.blank()));
    }

    public boolean isDeleted() {
        return deleted != null && deleted;
    }
}
