package net.trilogy.arch.adapter.structurizr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.structurizr.Workspace;
import com.structurizr.documentation.Decision;
import com.structurizr.documentation.Image;
import com.structurizr.documentation.Section;
import com.structurizr.model.Component;
import com.structurizr.model.Element;
import com.structurizr.model.Enterprise;
import com.structurizr.model.Location;
import com.structurizr.model.Model;
import com.structurizr.model.Person;
import com.structurizr.model.Relationship;
import com.structurizr.util.WorkspaceUtils;
import com.structurizr.view.ViewSet;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.domain.DocumentationImage;
import net.trilogy.arch.domain.DocumentationSection;
import net.trilogy.arch.domain.ImportantTechnicalDecision;
import net.trilogy.arch.domain.c4.C4Action;
import net.trilogy.arch.domain.c4.C4Component;
import net.trilogy.arch.domain.c4.C4Container;
import net.trilogy.arch.domain.c4.C4DeploymentNode;
import net.trilogy.arch.domain.c4.C4Location;
import net.trilogy.arch.domain.c4.C4Model;
import net.trilogy.arch.domain.c4.C4Path;
import net.trilogy.arch.domain.c4.C4Person;
import net.trilogy.arch.domain.c4.C4Relationship;
import net.trilogy.arch.domain.c4.C4SoftwareSystem;
import net.trilogy.arch.domain.c4.C4Tag;
import net.trilogy.arch.transformation.DeploymentNodeTransformer;

import java.io.File;
import java.util.List;
import java.util.Set;

import static com.structurizr.documentation.DecisionStatus.Proposed;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static net.trilogy.arch.domain.DocumentationSection.Format.ASCIIDOC;
import static net.trilogy.arch.domain.DocumentationSection.Format.MARKDOWN;
import static net.trilogy.arch.domain.c4.C4Action.DELIVERS;
import static net.trilogy.arch.domain.c4.C4Action.INTERACTS_WITH;
import static net.trilogy.arch.domain.c4.C4Action.USES;
import static net.trilogy.arch.domain.c4.C4Path.buildPath;

public class WorkspaceReader {
    public static ViewSet loadViews(File workspaceFile) throws Exception {
        Workspace workspace = WorkspaceUtils.loadWorkspaceFromJson(workspaceFile);
        return workspace.getViews();
    }

    private static List<C4DeploymentNode> deploymentNodes(Model model) {
        return model
                .getDeploymentNodes()
                .stream()
                .map(DeploymentNodeTransformer::toC4)
                .collect(toList());
    }

    private static ArchitectureDataStructure build(Workspace workspace) {
        ArchitectureDataStructure result = new ArchitectureDataStructure();
        result.setName(workspace.getName());

        Enterprise enterprise = workspace.getModel().getEnterprise();
        if (enterprise != null) {
            result.setBusinessUnit(enterprise.getName());
        }

        String description = workspace.getDescription();
        if (description == null) {
            description = "";
        }
        result.setDescription(description);

        return result;
    }

    private static C4Location convertLocation(Location location) {
        return C4Location.valueOf(location.name().toUpperCase());
    }

    private static Set<C4Tag> convertTags(String tags) {
        return stream(tags.split(",")).map(C4Tag::new).collect(toSet());
    }

    private static List<String> getSrcMappings(Component co) {
        try {
            return List.of(new ObjectMapper().readValue(
                    co.getProperties().get("Source Code Mappings"),
                    String[].class));
        } catch (Exception ignored) {
        }
        return List.of();
    }

    private static List<C4Relationship> mapRelationships(Model model, Element fromElement, Set<Relationship> relationships) {
        return relationships
                .stream()
                .map(r -> {
                    String destinationId = r.getDestination().getId();
                    Element element = model.getElement(destinationId);
                    C4Action action = convertAction(fromElement, element);
                    return new C4Relationship(r.getId(), null, action, null, destinationId, r.getDescription(), r.getTechnology());
                })
                .collect(toList());
    }

    private static C4Action convertAction(Element source, Element destination) {
        if (source instanceof Person && destination instanceof Person) {
            return INTERACTS_WITH;
        } else if (destination instanceof Person) {
            return DELIVERS;
        } else {
            return USES;
        }
    }

    private static List<ImportantTechnicalDecision> decisions(Workspace workspace) {
        Set<Decision> decisions = workspace.getDocumentation().getDecisions();
        return decisions.stream().map(d -> ImportantTechnicalDecision.builder()
                .content(d.getContent())
                .date(d.getDate())
                .id(d.getId())
                .status(ofNullable(d.getStatus()).orElse(Proposed).toString())
                .title(d.getTitle())
                .build())
                .collect(toList());
    }

    private static List<DocumentationSection> documentation(Workspace workspace) {
        Set<Section> docs = workspace.getDocumentation().getSections();
        return docs.stream().map(d -> new DocumentationSection(
                d.getElementId(),
                d.getTitle(),
                d.getOrder(),
                getFormat(d),
                d.getContent()))
                .collect(toList());
    }

    private static DocumentationSection.Format getFormat(Section d) {
        return d.getFormat().toString().equals("Markdown")
                ? MARKDOWN
                : ASCIIDOC;
    }

    private static List<DocumentationImage> documentationImages(Workspace workspace) {
        Set<Image> images = workspace.getDocumentation().getImages();
        return images.stream().map(i -> new DocumentationImage(
                i.getName(),
                i.getType(),
                i.getContent()))
                .collect(toList());
    }

    public static ArchitectureDataStructure loadWorkspace(File workspaceFile) throws Exception {
        Workspace workspace = WorkspaceUtils.loadWorkspaceFromJson(workspaceFile);
        ArchitectureDataStructure architectureDataStructure = build(workspace);
        Model model = workspace.getModel();

        C4Model c4Model = new C4Model();
        people(model).forEach(c4Model::addPerson);
        softwareSystems(model).forEach(c4Model::addSoftwareSystem);
        containers(model).forEach(c4Model::addContainer);
        components(model).forEach(c4Model::addComponent);
        deploymentNodes(model).forEach(c4Model::addDeploymentNode);
        architectureDataStructure.setModel(c4Model);

        List<ImportantTechnicalDecision> decisions = decisions(workspace);
        architectureDataStructure.setDecisions(decisions);

        List<DocumentationSection> documentation = documentation(workspace);
        architectureDataStructure.setDocumentation(documentation);

        List<DocumentationImage> documentationImages = documentationImages(workspace);
        architectureDataStructure.setDocumentationImages(documentationImages);

        return architectureDataStructure;
    }

    private static Set<C4Component> components(Model model) {
        return model
                .getSoftwareSystems()
                .stream()
                .flatMap(sys -> sys.getContainers()
                        .stream()
                        .flatMap(cont -> cont.getComponents()
                                .stream()
                                .map(co -> {
                                    C4Path c4Path = buildPath(co);

                                    Set<C4Tag> tags = convertTags(co.getTags());
                                    List<C4Relationship> relationships = mapRelationships(model, co, co.getRelationships());

                                    return C4Component.builder()
                                            .srcMappings(getSrcMappings(co))
                                            .id(co.getId())
                                            .containerId(cont.getId())
                                            .path(c4Path)
                                            .technology(co.getTechnology())
                                            .description(co.getDescription())
                                            .tags(tags)
                                            .name(co.getName())
                                            .relationships(relationships)
                                            .url(co.getUrl())
                                            .build();
                                })))
                .collect(toSet());
    }

    private static Set<C4Container> containers(Model model) {
        return model
                .getSoftwareSystems()
                .stream()
                .flatMap(sys -> sys.getContainers().stream().map(c -> {
                    List<C4Relationship> relationships = mapRelationships(model, c, c.getRelationships());
                    Set<C4Tag> tags = convertTags(c.getTags());

                    C4Path path = buildPath(c);
                    return C4Container.builder()
                            .id(c.getId())
                            .systemId(sys.getId())
                            .path(path)
                            .technology(c.getTechnology())
                            .description(c.getDescription())
                            .tags(tags)
                            .name(c.getName())
                            .relationships(relationships)
                            .url(c.getUrl())
                            .build();
                }))
                .collect(toSet());
    }

    private static Set<C4SoftwareSystem> softwareSystems(Model model) {
        return model
                .getSoftwareSystems()
                .stream()
                .map(sys -> {
                    List<C4Relationship> relationships = mapRelationships(model, sys, sys.getRelationships());
                    Set<C4Tag> tags = convertTags(sys.getTags());
                    C4Path path = buildPath(sys);

                    return C4SoftwareSystem.builder()
                            .id(sys.getId())
                            .path(path)
                            .description(sys.getDescription())
                            .location(convertLocation(sys.getLocation()))
                            .tags(tags)
                            .name(sys.getName())
                            .relationships(relationships)
                            .build();
                })
                .collect(toSet());
    }

    private static Set<C4Person> people(Model model) {
        return model
                .getPeople()
                .stream()
                .map(p -> {
                    List<C4Relationship> relationships = mapRelationships(model, p, p.getRelationships());
                    Set<C4Tag> tags = convertTags(p.getTags());
                    C4Path path = buildPath(p);

                    return C4Person.builder()
                            .id(p.getId())
                            .path(path)
                            .description(p.getDescription())
                            .location(convertLocation(p.getLocation()))
                            .tags(tags)
                            .name(p.getName())
                            .relationships(relationships)
                            .build();
                })
                .collect(toSet());
    }
}
