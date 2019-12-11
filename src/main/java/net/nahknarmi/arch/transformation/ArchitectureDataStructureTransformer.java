package net.nahknarmi.arch.transformation;

import com.structurizr.Workspace;
import com.structurizr.documentation.AutomaticDocumentationTemplate;
import com.structurizr.documentation.DecisionStatus;
import com.structurizr.model.Model;
import com.structurizr.model.Person;
import com.structurizr.model.SoftwareSystem;
import com.structurizr.view.SystemContextView;
import com.structurizr.view.ViewSet;
import net.nahknarmi.arch.model.*;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.structurizr.documentation.DecisionStatus.Deprecated;
import static com.structurizr.documentation.DecisionStatus.*;
import static com.structurizr.documentation.Format.Markdown;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

public class ArchitectureDataStructureTransformer {
    private final File documentationRoot;

    public ArchitectureDataStructureTransformer(File documentationRoot) {
        this.documentationRoot = documentationRoot;
    }

    public Workspace toWorkSpace(ArchitectureDataStructure dataStructure) throws IOException {
        checkNotNull(dataStructure, "ArchitectureDataStructure must not be null.");

        Workspace workspace = new Workspace(dataStructure.getName(), dataStructure.getDescription());
        workspace.setId(dataStructure.getId());

        addDocumentation(workspace, dataStructure);
        addDecisions(workspace, dataStructure);

        Model model = workspace.getModel();
        C4Model dataStructureModel = dataStructure.getModel();
        dataStructureModel.getPersons().forEach(p -> model.addPerson(p.getName(), p.getDescription()));
        dataStructureModel.getSystems().forEach(s -> model.addSoftwareSystem(s.getName(), s.getDescription()));
        dataStructureModel.relationships().forEach(r -> {
            String fromName = r.getFrom().getName();
            String toName = r.getTo().getName();

            if (r.getFrom() instanceof C4Person) {
                processPerson(model, r, fromName, toName);
            } else if (r.getFrom() instanceof C4SoftwareSystem) {
                processSoftwareSystem(model, r, fromName, toName);
            } else {
                throw new IllegalStateException("Unsupported type - " + r.getFrom().getClass());
            }
        });

        ViewSet viewSet = workspace.getViews();

        model.getSoftwareSystems().forEach(ss -> {
            SystemContextView context = viewSet.createSystemContextView(ss, "context" + new Random().nextInt(), ss.getName() + " Diagram");
            context.addAllSoftwareSystems();
            context.addAllPeople();
        });

        return workspace;
    }

    private void processPerson(Model model, C4Relationship r, String fromName, String toName) {
        Person fromPerson = ofNullable(model.getPersonWithName(fromName))
                .orElseThrow(() -> new IllegalStateException("Person with name " + fromName + " not found."));

        switch (r.getRelationshipType()){
            case USES:
                SoftwareSystem toSystem = ofNullable(model.getSoftwareSystemWithName(toName))
                        .orElseThrow(() -> new IllegalStateException("System with name " + toName + " not found."));

                fromPerson.uses(toSystem, "uses");
                break;

            case INTERACTS_WITH: //person to person relationship
                Person interactsWith = ofNullable(model.getPersonWithName(toName))
                        .orElseThrow(() -> new IllegalStateException("Person with name " + fromName + " not found."));

                fromPerson.interactsWith(interactsWith, "interacts with");
                break;

            case DELIVERS: //person to person relationship
                Person deliversTo = ofNullable(model.getPersonWithName(toName))
                        .orElseThrow(() -> new IllegalStateException("Person with name " + fromName + " not found."));

                fromPerson.delivers(deliversTo, "delivers");
                break;

            default:
                throw new IllegalStateException("Unexpected relationship type " + r.getRelationshipType());
        }
    }

    private void processSoftwareSystem(Model model, C4Relationship r, String fromName, String toName) {
        SoftwareSystem fromSystem = ofNullable(model.getSoftwareSystemWithName(fromName))
                .orElseThrow(() -> new IllegalStateException("SoftwareSystem with name " + fromName + " not found."));

        switch (r.getRelationshipType()){
            case USES:
                SoftwareSystem toSystem = ofNullable(model.getSoftwareSystemWithName(toName))
                        .orElseThrow(() -> new IllegalStateException("System with name " + toName + " not found."));

                fromSystem.uses(toSystem, "uses");
                break;

            case DELIVERS:
                Person deliversTo = ofNullable(model.getPersonWithName(toName))
                        .orElseThrow(() -> new IllegalStateException("System with name " + fromName + " not found."));

                fromSystem.delivers(deliversTo, "delivers");
                break;

            default:
                throw new IllegalStateException("Unexpected relationship type " + r.getRelationshipType());
        }
    }

    private void addDocumentation(Workspace workspace, ArchitectureDataStructure dataStructure) throws IOException {
        new AutomaticDocumentationTemplate(workspace).addSections(documentationPath(dataStructure));
    }

    private void addDecisions(Workspace workspace, ArchitectureDataStructure dataStructure) {
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

    private File documentationPath(ArchitectureDataStructure dataStructure) {
        String path = String.format("%s/%s/documentation/", documentationRoot.getAbsolutePath(), dataStructure.getName().toLowerCase());
        return new File(path);
    }
}
