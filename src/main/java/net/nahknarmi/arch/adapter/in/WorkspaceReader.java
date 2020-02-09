package net.nahknarmi.arch.adapter.in;

import com.structurizr.Workspace;
import com.structurizr.model.*;
import com.structurizr.util.WorkspaceUtils;
import com.structurizr.view.StaticView;
import com.structurizr.view.ViewSet;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.*;
import net.nahknarmi.arch.domain.c4.view.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static net.nahknarmi.arch.domain.c4.C4Action.*;
import static net.nahknarmi.arch.domain.c4.C4Path.buildPath;
import static net.nahknarmi.arch.domain.c4.C4Type.person;

public class WorkspaceReader {

    public ArchitectureDataStructure load(File workspaceFile) throws Exception {
        Workspace workspace = WorkspaceUtils.loadWorkspaceFromJson(workspaceFile);
        ArchitectureDataStructure architectureDataStructure = build(workspace);
        Model model = workspace.getModel();

        C4Model c4Model = new C4Model();
        people(model).forEach(c4Model::addPerson);
        softwareSystems(model).forEach(c4Model::addSoftwareSystem);
        containers(model).forEach(c4Model::addContainer);
        components(model).forEach(c4Model::addComponent);
        architectureDataStructure.setModel(c4Model);

        C4ViewContainer views = new C4ViewContainer();
        ViewSet workspaceViews = workspace.getViews();
        views.setSystemViews(systemViews(workspaceViews));
        views.setContainerViews(containerViews(workspaceViews));
        views.setComponentViews(componentViews(workspaceViews));
        architectureDataStructure.setViews(views);
        return architectureDataStructure;
    }

    private List<C4SystemView> systemViews(ViewSet views) {
        return views
                .getSystemContextViews()
                .stream()
                .map(x -> {
                    SoftwareSystem softwareSystem = x.getSoftwareSystem();
                    C4Path c4Path = buildPath(softwareSystem);
                    C4SystemView c4SystemView = new C4SystemView(c4Path);
                    mapCommonViewAttributes(x, c4SystemView);


                    return c4SystemView;
                })
                .collect(toList());
    }

    private List<C4ContainerView> containerViews(ViewSet views) {
        return views
                .getContainerViews()
                .stream()
                .map(x -> {
                    SoftwareSystem softwareSystem = x.getSoftwareSystem();
                    C4Path c4Path = buildPath(softwareSystem);
                    C4ContainerView view = new C4ContainerView(c4Path);
                    mapCommonViewAttributes(x, view);

                    return view;
                })
                .collect(toList());
    }

    private List<C4ComponentView> componentViews(ViewSet views) {
        return views
                .getComponentViews()
                .stream()
                .map(x -> {
                    Container container = x.getContainer();
                    C4Path c4Path = buildPath(container);
                    C4ComponentView view = new C4ComponentView(c4Path);
                    mapCommonViewAttributes(x, view);

                    return view;
                })
                .collect(toList());
    }

    private void mapCommonViewAttributes(StaticView view, C4View c4View) {
        c4View.setName(view.getName());
        c4View.setDescription(view.getDescription());
        c4View.setKey(view.getKey());
        List<C4Path> elements = view.getElements().stream().map(y -> buildPath(y.getElement())).collect(toList());
        c4View.setEntities(elements);
    }

    private Set<C4Component> components(Model model) {
        return model
                .getSoftwareSystems()
                .stream()
                .flatMap(s -> s.getContainers()
                        .stream()
                        .flatMap(c -> c.getComponents()
                                .stream()
                                .map(co -> {
                                    C4Path c4Path = buildPath(co);

                                    Set<C4Tag> tags = convertTags(co.getTags());
                                    List<C4Relationship> relationships = mapRelationships(co, co.getRelationships());
                                    return C4Component.builder()
                                            .path(c4Path)
                                            .technology(co.toString())
                                            .description(co.getDescription())
                                            .tags(tags)
                                            .name(co.getName())
                                            .relationships(relationships)
                                            .url(co.getUrl())
                                            .build();
                                })

                        )

                ).collect(toSet());
    }

    private Set<C4Container> containers(Model model) {
        return model
                .getSoftwareSystems()
                .stream()
                .flatMap(s -> s.getContainers().stream().map(c -> {
                    List<C4Relationship> relationships = mapRelationships(c, c.getRelationships());
                    Set<C4Tag> tags = convertTags(c.getTags());

                    C4Path path = buildPath(c);
                    return C4Container
                            .builder()
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

    private Set<C4SoftwareSystem> softwareSystems(Model model) {
        return model
                .getSoftwareSystems()
                .stream()
                .map(x -> {
                    List<C4Relationship> relationships = mapRelationships(x, x.getRelationships());
                    Set<C4Tag> tags = convertTags(x.getTags());
                    C4Path path = buildPath(x);

                    return C4SoftwareSystem
                            .builder()
                            .path(path)
                            .description(x.getDescription())
                            .location(convertLocation(x.getLocation()))
                            .tags(tags)
                            .name(x.getName())
                            .relationships(relationships)
                            .build();
                })
                .collect(toSet());
    }

    private Set<C4Person> people(Model model) {
        return model
                .getPeople()
                .stream()
                .map(x -> {
                    List<C4Relationship> relationships = mapRelationships(x, x.getRelationships());
                    Set<C4Tag> tags = convertTags(x.getTags());
                    C4Path path = buildPath(x);

                    return C4Person.builder()
                            .path(path)
                            .description(x.getDescription())
                            .location(convertLocation(x.getLocation()))
                            .tags(tags)
                            .name(x.getName())
                            .relationships(relationships)
                            .build();
                })
                .collect(toSet());
    }

    private ArchitectureDataStructure build(Workspace workspace) {
        ArchitectureDataStructure result = new ArchitectureDataStructure();
        result.setName(workspace.getName());
        result.setBusinessUnit(workspace.getModel().getEnterprise().getName());
        result.setDescription(workspace.getDescription());
        return result;
    }

    private C4Location convertLocation(Location location) {
        return C4Location.valueOf(location.name().toUpperCase());
    }

    private Set<C4Tag> convertTags(String tags) {
        return Arrays.stream(tags.split(",")).map(C4Tag::new).collect(toSet());
    }

    private List<C4Relationship> mapRelationships(Element fromElement, Set<Relationship> relationships) {
        return relationships
                .stream()
                .map(r -> {
                    C4Path destination = buildPath(r.getDestination());
                    C4Action action = convertAction(fromElement, destination);
                    return new C4Relationship(action, destination, r.getDescription(), r.getTechnology());
                }).collect(toList());
    }

    private C4Action convertAction(Element fromElement, C4Path destination) {
        C4Action action;
        if (fromElement instanceof Person && destination.type().equals(person)){
            action = INTERACTS_WITH;
        } else if (destination.type().equals(person)) {
            action = DELIVERS;
        } else {
            action = USES;
        }
        return action;
    }

}
