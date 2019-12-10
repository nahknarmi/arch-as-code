package net.nahknarmi.arch.transformation;

import com.structurizr.Workspace;
import com.structurizr.documentation.AutomaticDocumentationTemplate;
import com.structurizr.documentation.DecisionStatus;
import com.structurizr.documentation.Documentation;
import com.structurizr.documentation.Format;
import net.nahknarmi.arch.model.ArchitectureDataStructure;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static com.structurizr.documentation.DecisionStatus.Deprecated;
import static com.structurizr.documentation.DecisionStatus.*;
import static java.util.Optional.ofNullable;

public class ArchitectureDataStructureTransformer {

    public Workspace toWorkSpace(ArchitectureDataStructure dataStructure) throws IOException {
        Workspace workspace = new Workspace(dataStructure.getName(), dataStructure.getDescription());
        workspace.setId(dataStructure.getId());
        String productName = dataStructure.getName().toLowerCase();

        addDocumentation(workspace, productName);
        addDecisions(workspace, dataStructure);

        return workspace;
    }

    private void addDocumentation(Workspace workspace, String productName) throws IOException {
        AutomaticDocumentationTemplate template = new AutomaticDocumentationTemplate(workspace);
        URL documentationResource = getClass().getResource(String.format("/architecture/products/%s/documentation/", productName));
        template.addSections(new File(documentationResource.getPath()));
    }

    private void addDecisions(Workspace workspace, ArchitectureDataStructure dataStructure) {
        if (dataStructure.getDecisions() != null) {
            Documentation documentation = workspace.getDocumentation();
            dataStructure
                    .getDecisions()
                    .forEach(d -> documentation.addDecision(d.getId(), d.getDate(), d.getTitle(), getDecisionStatus(d.getStatus()), Format.Markdown, d.getContent()));
        }
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
