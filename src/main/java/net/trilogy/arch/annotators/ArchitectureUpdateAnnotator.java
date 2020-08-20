package net.trilogy.arch.annotators;

import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArchitectureUpdateAnnotator {
    private static final String REGEX = "(\\n\\s*-\\s['\\\"]?component-id['\\\"]?:\\s+['\\\"]?([a-zA-Z\\d]+)['\\\"]?).*((^\\n)*\\n)";
    public static final int ORIGINAL_LINE = 1;
    public static final int COMPONENT_ID = 2;
    public static final int TRAILING_WHITESPACE = 3;

    public String annotateC4Paths(ArchitectureDataStructure dataStructure, String auAsString) {
        Pattern regexToGetComponentReferences = Pattern.compile(REGEX);
        final Matcher matcher = regexToGetComponentReferences.matcher(auAsString);

        String annotatedAu = null;
        while (matcher.find()) {
            annotatedAu = matcher.replaceAll((res) ->
                    res.group(ORIGINAL_LINE) +
                            getComponentPathComment(dataStructure, res.group(COMPONENT_ID)) +
                            res.group(TRAILING_WHITESPACE)
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

    public String getComponentPathComment(ArchitectureDataStructure architecture, String id) {
        try {
            return "  # " + architecture.getModel().findEntityById(id).get().getPath().getPath();
        } catch (Exception ignored) {
            return "";
        }
    }
}
