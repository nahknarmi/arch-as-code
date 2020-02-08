package net.nahknarmi.arch.generator;

import com.structurizr.model.Element;
import com.structurizr.model.IdGenerator;
import com.structurizr.model.Relationship;
import lombok.NonNull;
import net.nahknarmi.arch.domain.c4.C4Model;
import net.nahknarmi.arch.domain.c4.C4Type;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PathIdGenerator implements IdGenerator {
    private final C4Model dataStructureModel;

    public PathIdGenerator(@NonNull C4Model dataStructureModel) {
        this.dataStructureModel = dataStructureModel;
    }

    @Override
    public String generateId(Element element) {
        C4Type c4Type = C4Type.from(element);

        return dataStructureModel
                .allEntities()
                .stream()
                .filter(e -> e.getPath().getType().equals(c4Type))
                .filter(x -> x.getName().equals(element.getName()))
                .findFirst()
                .map(p -> p.getPath().getPath())
                .orElseThrow(() -> new IllegalStateException("Unable to find " + c4Type.name() + " with name '" + element.getName() + "'."));
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
