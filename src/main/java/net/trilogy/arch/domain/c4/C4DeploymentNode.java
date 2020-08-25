package net.trilogy.arch.domain.c4;

import lombok.*;

import java.util.Set;
import java.util.TreeSet;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class C4DeploymentNode extends Entity {
    private String technology;
    private String environment;
    private Integer instances;
    private Set<C4DeploymentNode> children = new TreeSet<>();
    private Set<C4ContainerInstance> containerInstances = new TreeSet<>();

    @Builder(toBuilder = true)
    C4DeploymentNode(String id,
                     String alias,
                     String name,
                     C4Path path,
                     String description,
                     @Singular Set<C4Tag> tags,
                     @Singular Set<C4Relationship> relationships,
                     String technology,
                     String environment,
                     Integer instances,
                     Set<C4DeploymentNode> children,
                     Set<C4ContainerInstance> containerInstances) {
        super(id, alias, path, name, description, tags, relationships);
        this.technology = technology;
        this.environment = environment;
        this.instances = instances;
        this.children = children;
        this.containerInstances = containerInstances;
    }

    public void addChild(C4DeploymentNode node) {
        this.children.add(node);
    }

    public void addContainerInstance(C4ContainerInstance containerInstance) {
        this.containerInstances.add(containerInstance);
    }

    public C4Type getType() {
        return C4Type.DEPLOYMENT_NODE;
    }

    @Override
    public C4DeploymentNode shallowCopy() {
        return this.toBuilder().build();
    }
}
