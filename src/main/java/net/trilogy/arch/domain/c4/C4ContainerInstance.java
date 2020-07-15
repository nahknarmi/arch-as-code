package net.trilogy.arch.domain.c4;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class C4ContainerInstance implements Comparable<C4ContainerInstance> {
    private String id;
    private String environment;
    @NonNull
    private C4Reference containerReference;
    private Integer instanceId = 1;

    @Override
    public int compareTo(C4ContainerInstance other) {
        return this.getId().compareTo(other.getId());
    }
}
