package net.trilogy.arch.domain.architectureUpdate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

import java.util.Map;

@ToString
@EqualsAndHashCode
public class TddContainerByComponent {
    @Getter
    @JsonProperty(value = "component-id")
    private final Tdd.ComponentReference componentId;

    @Getter
    @JsonProperty(value = "component-path")
    private final String componentPath;

    @Getter 
    @JsonProperty(value = "tdds") 
    private final Map<Tdd.Id, Tdd> tdds;

    @JsonProperty(value = "deleted") 
    private final Boolean deleted;

    @Builder(toBuilder = true)
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public TddContainerByComponent(
            @JsonProperty(value = "component-id") Tdd.ComponentReference componentId,
            @JsonProperty(value = "component-path") String componentPath,
            @JsonProperty(value = "deleted") Boolean deleted,
            @JsonProperty(value = "tdds") Map<Tdd.Id, Tdd> tdds
    ) {
        this.componentId = componentId;
        this.componentPath = componentPath;
        this.deleted = deleted;
        this.tdds = tdds;
    }

    public boolean isDeleted() {
        return deleted != null && deleted;
    }

    public static TddContainerByComponent blank() {
        return new TddContainerByComponent(Tdd.ComponentReference.blank(), null, false, Map.of(Tdd.Id.blank(), Tdd.blank()));
    }
}
