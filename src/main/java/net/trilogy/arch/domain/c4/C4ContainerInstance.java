package net.trilogy.arch.domain.c4;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class C4ContainerInstance implements Comparable<C4ContainerInstance> {
    private String id;
    private String environment;
    @NonNull
    private C4Reference containerReference;
    private Integer instanceId = 1;

    @Override
    public int compareTo(C4ContainerInstance other) {
        return getId().compareTo(other.getId());
    }
}
