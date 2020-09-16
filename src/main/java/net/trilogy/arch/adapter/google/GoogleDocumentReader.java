package net.trilogy.arch.adapter.google;

import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.Decision;
import net.trilogy.arch.domain.architectureUpdate.Decision.DecisionId;
import net.trilogy.arch.domain.architectureUpdate.Jira;
import net.trilogy.arch.domain.architectureUpdate.P1;
import net.trilogy.arch.domain.architectureUpdate.P2;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddId;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GoogleDocumentReader {
    private final GoogleDocsFacade api;

    public GoogleDocumentReader(GoogleDocsFacade api) {
        this.api = api;
    }

    private static Map<DecisionId, Decision> extractDecisions(GoogleDocsJsonParser jsonParser) {
        var map = new LinkedHashMap<DecisionId, Decision>();
        jsonParser.getDecisions().forEach(decisionString -> {
            String[] split = decisionString.split("-", 2);
            if (split.length == 2) {
                var id = new DecisionId(split[0].trim());
                var requirement = new Decision(split[1].trim(), List.of(TddId.blank()));
                map.put(id, requirement);
            }
        });
        return map;
    }

    private static P2 extractP2(GoogleDocsJsonParser jsonParser) {
        return P2.builder()
                .link(jsonParser.getP2Link().orElse(""))
                .build();
    }

    private static P1 extractP1(GoogleDocsJsonParser jsonParser, String url) {
        return P1.builder()
                .link(url)
                .executiveSummary(jsonParser.getExecutiveSummary().orElse(""))
                .jira(new Jira(
                                jsonParser.getP1JiraTicket().orElse(""),
                                jsonParser.getP1JiraLink().orElse("")
                        )
                ).build();
    }

    private static boolean isEmpty(GoogleDocsFacade.Response response) {
        return !response.asJson().hasNonNull("body");
    }

    public ArchitectureUpdate load(String url) throws IOException {
        var response = api.fetch(url);

        if (isEmpty(response)) {
            return ArchitectureUpdate.blank();
        }

        GoogleDocsJsonParser jsonParser = new GoogleDocsJsonParser(response.asJson());

        return ArchitectureUpdate.prefilledWithBlanks()
                .milestone(jsonParser.getMilestone().orElse(""))
                .p1(extractP1(jsonParser, url))
                .p2(extractP2(jsonParser))
                .decisions(extractDecisions(jsonParser))
                .build();
    }
}
