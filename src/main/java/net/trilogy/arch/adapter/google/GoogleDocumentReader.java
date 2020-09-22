package net.trilogy.arch.adapter.google;

import net.trilogy.arch.domain.architectureUpdate.YamlArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.YamlDecision;
import net.trilogy.arch.domain.architectureUpdate.YamlDecision.DecisionId;
import net.trilogy.arch.domain.architectureUpdate.YamlJira;
import net.trilogy.arch.domain.architectureUpdate.YamlP1;
import net.trilogy.arch.domain.architectureUpdate.YamlP2;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddId;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GoogleDocumentReader {
    private final GoogleDocsFacade api;

    public GoogleDocumentReader(GoogleDocsFacade api) {
        this.api = api;
    }

    private static Map<DecisionId, YamlDecision> extractDecisions(GoogleDocsJsonParser jsonParser) {
        var map = new LinkedHashMap<DecisionId, YamlDecision>();
        jsonParser.getDecisions().forEach(decisionString -> {
            String[] split = decisionString.split("-", 2);
            if (split.length == 2) {
                var id = new DecisionId(split[0].trim());
                var requirement = new YamlDecision(split[1].trim(), List.of(TddId.blank()));
                map.put(id, requirement);
            }
        });
        return map;
    }

    private static YamlP2 extractP2(GoogleDocsJsonParser jsonParser) {
        return YamlP2.builder()
                .link(jsonParser.getP2Link().orElse(""))
                .build();
    }

    private static YamlP1 extractP1(GoogleDocsJsonParser jsonParser, String url) {
        return YamlP1.builder()
                .link(url)
                .executiveSummary(jsonParser.getExecutiveSummary().orElse(""))
                .jira(new YamlJira(
                                jsonParser.getP1JiraTicket().orElse(""),
                                jsonParser.getP1JiraLink().orElse("")
                        )
                ).build();
    }

    private static boolean isEmpty(GoogleDocsFacade.Response response) {
        return !response.asJson().hasNonNull("body");
    }

    public YamlArchitectureUpdate load(String url) throws IOException {
        var response = api.fetch(url);

        if (isEmpty(response)) {
            return YamlArchitectureUpdate.blank();
        }

        GoogleDocsJsonParser jsonParser = new GoogleDocsJsonParser(response.asJson());

        return YamlArchitectureUpdate.prefilledYamlArchitectureUpdateWithBlanks()
                .milestone(jsonParser.getMilestone().orElse(""))
                .p1(extractP1(jsonParser, url))
                .p2(extractP2(jsonParser))
                .decisions(extractDecisions(jsonParser))
                .build();
    }
}
