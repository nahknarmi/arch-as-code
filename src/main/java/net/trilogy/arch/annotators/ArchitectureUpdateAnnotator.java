package net.trilogy.arch.annotators;

import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.Tdd;
import net.trilogy.arch.domain.architectureUpdate.TddContainerByComponent;
import net.trilogy.arch.domain.architectureUpdate.TddContent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

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

    public ArchitectureUpdate annotateTddContentFiles(ArchitectureUpdate au) {
        var tddContainers = au.getTddContainersByComponent().stream()
                .map(c -> new TddContainerByComponent(
                                c.getComponentId(),
                                c.isDeleted(),
                                c.getTdds().entrySet().stream().collect(toMap(
                                        Map.Entry::getKey,
                                        (tdd) -> addFileNameToTdd(c.getComponentId(), tdd, au.getTddContents())))
                        )
                ).collect(toList());

        return au.toBuilder().tddContainersByComponent(tddContainers).build();
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

    private Tdd addFileNameToTdd(Tdd.ComponentReference id, Map.Entry<Tdd.Id, Tdd> pair, List<TddContent> tddContents) {
        String tddId = pair.getKey().toString();
        Tdd tdd = pair.getValue();
        Optional<TddContent> found = tddContents.stream()
                .filter(tc -> tc.getComponentId().equals(id.getId()))
                .filter(tc -> tc.getTdd().equals(tddId))
                .findFirst();

        return found.map(tddContent -> new Tdd(tdd.getText(), tddContent.getFilename())).orElse(tdd);
    }
}
