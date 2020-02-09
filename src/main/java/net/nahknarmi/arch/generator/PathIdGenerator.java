package net.nahknarmi.arch.generator;

import com.structurizr.model.Element;
import com.structurizr.model.IdGenerator;
import com.structurizr.model.Relationship;
import lombok.NonNull;
import net.nahknarmi.arch.domain.c4.C4Model;
import net.nahknarmi.arch.domain.c4.C4Type;
import net.nahknarmi.arch.domain.c4.Entity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class PathIdGenerator implements IdGenerator {
    private final C4Model dataStructureModel;

    public PathIdGenerator(@NonNull C4Model dataStructureModel) {
        this.dataStructureModel = dataStructureModel;
    }

    @Override
    public String generateId(Element element) {
        C4Type c4Type = C4Type.from(element);

        List<@NonNull Entity> possibleEntities = dataStructureModel
                .allEntities()
                .stream()
                .filter(e -> e.getPath().type().equals(c4Type))
                .filter(x -> x.getName().equals(element.getName()))
                .filter(entity -> {
                    switch (c4Type) {
                        case container: {
                            Element elementSystem = element.getParent();
                            Entity entitySystem = dataStructureModel.findByPath(entity.getPath().systemPath());

                            return elementSystem.getName().equals(entitySystem.getName());
                        }
                        case component: {
                            Element elementContainer = element.getParent();
                            Element elementSystem = elementContainer.getParent();

                            Entity entityContainer = dataStructureModel.findByPath(entity.getPath().containerPath());
                            Entity entitySystem = dataStructureModel.findByPath(entity.getPath().systemPath());

                            return elementSystem.getName().equals(entitySystem.getName())
                                    && elementContainer.getName().equals(entityContainer.getName());
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

        return possibleEntities.get(0).getPath().getPath();
    }

    @Override
    public String generateId(Relationship relationship) {
        String relationshipString = relationship.toString();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        byte[] hashInBytes = md.digest(relationshipString.getBytes(StandardCharsets.UTF_8));

        // bytes to hex
        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    @Override
    public void found(String id) {
    }

}
