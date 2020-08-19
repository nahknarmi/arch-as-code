package net.trilogy.arch.annotators;

import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.c4.Entity;
import net.trilogy.arch.facade.FilesFacade;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ArchitectureUpdateAnnotator {
    private static final String REGEX = "(\\n\\s*-\\s['\\\"]?component-id['\\\"]?:\\s+['\\\"]?(\\d+)['\\\"]?).*((^\\n)*\\n)";
    private Pattern regexToGetComponentReferences;

    public void annotateC4Paths(ArchitectureDataStructure dataStructure,
                                String auAsString,
                                Path auPath,
                                FilesFacade filesFacade) throws IOException {

        regexToGetComponentReferences = Pattern.compile(REGEX);
        final Matcher matcher = regexToGetComponentReferences.matcher(auAsString);

        while (matcher.find()) {
            auAsString = matcher.replaceAll((res) ->
                    res.group(1) +
                            getComponentPathComment(res.group(2), dataStructure) +
                            res.group(3)
            );
        }

        filesFacade.writeString(auPath, auAsString);
    }

    public Set<Entity> getComponentsToValidate(ArchitectureDataStructure dataStructure, ArchitectureUpdate au) {
        return au.getTddContainersByComponent().stream()
                .map(tdd -> dataStructure
                        .getModel()
                        .findEntityById(tdd.getComponentId().getId())
                )
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
    }

    private String getComponentPathComment(String id, ArchitectureDataStructure architecture) {
        try {
            return "  # " + architecture.getModel().findEntityById(id).orElseThrow(() -> new IllegalStateException("Could not find entity with id: " + id)).getPath().getPath();
        } catch (Exception ignored) {
            return "";
        }
    }
}
