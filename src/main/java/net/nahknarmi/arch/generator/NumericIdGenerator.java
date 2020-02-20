package net.nahknarmi.arch.generator;

import com.structurizr.model.Element;
import com.structurizr.model.IdGenerator;
import com.structurizr.model.Relationship;
import io.vavr.Tuple2;
import lombok.NonNull;
import net.nahknarmi.arch.domain.c4.C4Model;
import net.nahknarmi.arch.domain.c4.C4Relationship;
import net.nahknarmi.arch.domain.c4.C4Type;
import net.nahknarmi.arch.domain.c4.Entity;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

public class NumericIdGenerator implements IdGenerator {
    private final C4Model dataStructureModel;

    public NumericIdGenerator(@NonNull C4Model dataStructureModel) {
        this.dataStructureModel = dataStructureModel;
    }

    @Override
    public String generateId(Element element) {
        C4Type c4Type = C4Type.from(element);

        List<@NonNull Entity> possibleEntities = dataStructureModel
                .allEntities()
                .stream()
                .filter(e -> e.getPath().type().equals(c4Type))
                .filter(e -> e.name().equals(element.getName()))
                .filter(entity -> {
                    switch (c4Type) {
                        case container: {
                            Element elementSystem = element.getParent();
                            Entity entitySystem = dataStructureModel.findByPath(entity.getPath().systemPath());

                            return elementSystem.getName().equals(entitySystem.name());
                        }
                        case component: {
                            Element elementContainer = element.getParent();
                            Element elementSystem = elementContainer.getParent();

                            Entity entityContainer = dataStructureModel.findByPath(entity.getPath().containerPath());
                            Entity entitySystem = dataStructureModel.findByPath(entity.getPath().systemPath());

                            return elementSystem.getName().equals(entitySystem.name())
                                    && elementContainer.getName().equals(entityContainer.name());
                        }
                        case system:
                        case person:
                            return true;
                        default:
                            throw new IllegalStateException("Unsupported type " + c4Type);
                    }
                })
                .collect(toList());

        if (possibleEntities.isEmpty()) {
            throw new IllegalStateException("Entity could not be found for element " + element);
        }

        if (possibleEntities.size() > 1) {
            throw new IllegalStateException("More than 1 matching entity found for element " + element);
        }

        if (possibleEntities.isEmpty()) {
            return null;
        } else {
            return possibleEntities.get(0).getId();
        }
    }

    @Override
    public String generateId(Relationship relationship) {
        checkNotNull(relationship.getSourceId(), relationship);
        checkNotNull(relationship.getDestinationId(), relationship);

        List<Tuple2<Entity, C4Relationship>> possibleRelationships = dataStructureModel
                .allRelationships()
                .stream()
                .filter(t -> t._1.getId().equals(relationship.getSourceId()))
                .filter(t -> {
                    @NonNull String entityId = t._2.getWith();
                    return entityId.equals(relationship.getDestinationId());
                })
                .filter(t -> {
                    if (relationship.getTechnology() != null) {
                        return t._2.getTechnology().equals(relationship.getTechnology());
                    } else {
                        return true;
                    }
                })
                .collect(toList());

        if (possibleRelationships.isEmpty()) {
            return null;
        } else {
            return possibleRelationships.get(0)._2.getId();
        }
    }

    @Override
    public void found(String id) {
    }

}
