package net.trilogy.arch.transformation.enhancer;

import com.structurizr.Workspace;
import com.structurizr.documentation.DecisionStatus;
import net.trilogy.arch.domain.ArchitectureDataStructure;

import static com.structurizr.documentation.DecisionStatus.Accepted;
import static com.structurizr.documentation.DecisionStatus.Deprecated;
import static com.structurizr.documentation.DecisionStatus.Proposed;
import static com.structurizr.documentation.DecisionStatus.Rejected;
import static com.structurizr.documentation.DecisionStatus.Superseded;
import static com.structurizr.documentation.Format.Markdown;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

public class DecisionEnhancer implements WorkspaceEnhancer {
    @Override
    public void enhance(Workspace workspace, ArchitectureDataStructure dataStructure) {
        ofNullable(dataStructure.getDecisions())
                .orElse(emptyList())
                .forEach(d ->
                        workspace.getDocumentation()
                                .addDecision(d.getId(), d.getDate(), d.getTitle(), getDecisionStatus(d.getStatus()), Markdown, d.getContent()));
    }

    private DecisionStatus getDecisionStatus(String status) {
        DecisionStatus decisionStatus;
        switch (ofNullable(status).orElse(Proposed.name()).toLowerCase()) {
            case "accepted":
                decisionStatus = Accepted;
                break;
            case "superseded":
                decisionStatus = Superseded;
                break;
            case "deprecated":
                decisionStatus = Deprecated;
                break;
            case "rejected":
                decisionStatus = Rejected;
                break;
            default:
                decisionStatus = Proposed;
                break;
        }
        return decisionStatus;
    }
}
