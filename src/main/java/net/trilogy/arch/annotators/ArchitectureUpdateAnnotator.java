package net.trilogy.arch.annotators;

import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArchitectureUpdateAnnotator {
    private static final String REGEX = "(\\n\\s*-\\s['\\\"]?component-id['\\\"]?:\\s+['\\\"]?(\\d+)['\\\"]?).*((^\\n)*\\n)";

    public String annotateC4Paths(ArchitectureDataStructure dataStructure, String auAsString) {
        Pattern regexToGetComponentReferences = Pattern.compile(REGEX);
        final Matcher matcher = regexToGetComponentReferences.matcher(auAsString);

        String annotatedAu = null;
        while (matcher.find()) {
            annotatedAu = matcher.replaceAll((res) ->
                    res.group(1) +
                            getComponentPathComment(res.group(2), dataStructure) +
                            res.group(3)
            );
        }

        return annotatedAu;
    }

    public boolean isComponentsEmpty(ArchitectureDataStructure dataStructure, ArchitectureUpdate au) {
        return au.getTddContainersByComponent().stream()
                .flatMap(tdd -> dataStructure
                        .getModel()
                        .findEntityById(tdd.getComponentId().getId())
                        .stream()
                ).findAny()
                .isEmpty();
    }

    private String getComponentPathComment(String id, ArchitectureDataStructure architecture) {
        try {
            return "  # " + architecture.getModel().findEntityById(id).orElseThrow(() -> new IllegalStateException("Could not find entity with id: " + id)).getPath().getPath();
        } catch (Exception ignored) {
            return "";
        }
    }
}
