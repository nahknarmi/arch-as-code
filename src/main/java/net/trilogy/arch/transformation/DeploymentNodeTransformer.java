package net.trilogy.arch.transformation;

import com.structurizr.model.Container;
import com.structurizr.model.ContainerInstance;
import com.structurizr.model.DeploymentNode;
import com.structurizr.model.Model;
import net.trilogy.arch.domain.c4.C4ContainerInstance;
import net.trilogy.arch.domain.c4.C4DeploymentNode;
import net.trilogy.arch.domain.c4.C4Model;
import net.trilogy.arch.domain.c4.C4Reference;
import net.trilogy.arch.generator.FunctionalIdGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class DeploymentNodeTransformer {
    static public DeploymentNode addDeploymentNodeFromC4ToModel(C4DeploymentNode node, C4Model c4Model, Model model, FunctionalIdGenerator idGenerator) {
        idGenerator.setNext(node.getId());
        DeploymentNode deploymentNode = model.addDeploymentNode(node.getEnvironment(), node.getName(), node.getDescription(), node.getTechnology(), node.getInstances());
        addChildren(model, c4Model, deploymentNode, node, idGenerator);
        return deploymentNode;
    }

    private static void addChildren(Model model, C4Model c4Model, DeploymentNode deploymentNode, C4DeploymentNode c4Node, FunctionalIdGenerator idGenerator) {
        Set<C4ContainerInstance> containerInstances = c4Node.getContainerInstances();
        if (!containerInstances.isEmpty()) {
            containerInstances.forEach(instance -> {
                String containerId = c4Model.findEntityByReference(instance.getContainerReference()).getId();
                idGenerator.setNext(instance.getId());
                setIdGeneratorToHandleRelationships(idGenerator);
                deploymentNode.add((Container) Objects.requireNonNull(model.getElement(containerId)));
                idGenerator.clearDefaultForRelationships();
            });
        }

        Set<C4DeploymentNode> children = c4Node.getChildren();
        if (!children.isEmpty()) {
            children.forEach(child -> {
                idGenerator.setNext(child.getId());
                DeploymentNode addedDeploymentNode = deploymentNode.addDeploymentNode(
                        child.getName(),
                        child.getDescription(),
                        child.getTechnology(),
                        child.getInstances()
                );

                addChildren(model, c4Model, addedDeploymentNode, child, idGenerator);
            });
        }
    }

    private static void setIdGeneratorToHandleRelationships(FunctionalIdGenerator idGenerator) {
        Map<String, Integer> idCounts = new HashMap<>();
        idGenerator.setDefaultForRelationships((r) -> {
            String base = r.getSourceId() + "->" + r.getDestinationId();
            Integer count = idCounts.getOrDefault(base, 1);
            String id = base + ":" + count++;
            idCounts.put(base, count);
            return id;
        });
    }

    public static C4DeploymentNode toC4(DeploymentNode node) {
        C4DeploymentNode c4Node = C4DeploymentNode.builder()
                .id(node.getId())
                .name(node.getName())
                .environment(node.getEnvironment())
                .technology(node.getTechnology())
                .description(node.getDescription())
                .tags(Set.of())
                .instances(node.getInstances())
                .containerInstances(new TreeSet<>())
                .children(new TreeSet<>())
                .build();

        node.getContainerInstances().forEach(
                containerInstance -> c4Node.addContainerInstance(toC4(containerInstance))
        );

        node.getChildren().forEach(
                child -> c4Node.addChild(toC4(child))
        );

        return c4Node;
    }

    private static C4ContainerInstance toC4(ContainerInstance containerInstance) {
        return new C4ContainerInstance(containerInstance.getId(),
                containerInstance.getEnvironment(),
                new C4Reference(containerInstance.getContainerId(), null),
                containerInstance.getInstanceId());
    }
}
