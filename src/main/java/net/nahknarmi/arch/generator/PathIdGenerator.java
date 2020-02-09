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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PathIdGenerator implements IdGenerator {
    private final C4Model dataStructureModel;
    private final Map<Element, String> ids = new HashMap<>();

    public PathIdGenerator(@NonNull C4Model dataStructureModel) {
        this.dataStructureModel = dataStructureModel;
    }

    @Override
    public String generateId(Element element) {
        C4Type c4Type = C4Type.from(element);

        List<@NonNull String> possiblePaths = dataStructureModel
                .allEntities()
                .stream()
                .filter(e -> e.getPath().getType().equals(c4Type))
                .filter(x -> x.getName().equals(element.getName()))
                .filter(x -> {
                    if (c4Type.equals(C4Type.container)) {
                        Element elementSystem = element.getParent();

                        Entity systemOfX = dataStructureModel.findByPath(x.getPath().getSystemPath()).get();
                        boolean systemNameMatches = elementSystem.getName().equals(systemOfX.getName());

                        return systemNameMatches;
                    }

                    if (c4Type.equals(C4Type.component)) {
                        Element elementContainer = element.getParent();
                        Element elementSystem = elementContainer.getParent();

                        Entity containerOfX = dataStructureModel.findByPath(x.getPath().getContainerPath().get()).get();
                        Entity systemOfX = dataStructureModel.findByPath(x.getPath().getSystemPath()).get();

                        boolean systemNameMatches = elementSystem.getName().equals(systemOfX.getName());
                        boolean containerNameMatches = elementContainer.getName().equals(containerOfX.getName());

                        return systemNameMatches && containerNameMatches;
                    }

                    return true;
                })
                .map(p -> {
                    if ("c4://Sococo Virtual Office/iOS App/Ionic".equals(p.getPath().getPath())) {
                        System.err.println("--------" + element);
                    }

                    if (ids.containsValue(p.getPath().getPath())) {
                        List<Map.Entry<Element, String>> collect = ids.entrySet().stream().filter(x -> x.getValue().equals(p.getPath().getPath())).collect(Collectors.toList());
                        System.err.println("Heree!!!!");
                    } else {
                        ids.put(element, p.getPath().getPath());
                    }
                    return p.getPath().getPath();
                })
                .collect(Collectors.toList());
        if (possiblePaths.isEmpty()) {
            throw new IllegalStateException("Unable to build path");
        }

        if (possiblePaths.size() > 1) {
            throw new IllegalStateException("More than 1 path found.");
        }

//                .orElseThrow(() -> new IllegalStateException("Unable to find " + c4Type.name() + " with name '" + element.getName() + "'."));

        return possiblePaths.get(0);
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
