package net.trilogy.arch.services;

import com.google.common.annotations.VisibleForTesting;
import net.trilogy.arch.domain.c4.C4Type;
import net.trilogy.arch.domain.diff.Diff;
import net.trilogy.arch.domain.diff.DiffableEntity;
import net.trilogy.arch.domain.diff.DiffableRelationship;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class DiffToDotCalculator {

    public static String toDot(String title, Collection<Diff> diffs, @Nullable Diff parentEntityDiff, String linkPrefix) {
        final var dot = new Dot();
        dot.add(0, "digraph \"" + title + "\" {");
        dot.add(1, "graph [rankdir=LR];");
        dot.add(0, "");
        if (parentEntityDiff != null) {
            dot.add(1, "subgraph \"cluster_" + parentEntityDiff.getElement().getId() + "\" {");
            dot.add(2, "style=filled;");
            dot.add(2, "color=grey92;");
            dot.add(2, "label=\"" + parentEntityDiff.getElement().getName() + "\";");
            parentEntityDiff.getDescendants()
                    .stream()
                    .filter(it -> diffs.stream().anyMatch(diff -> it.getId().equals(diff.getElement().getId())))
                    .filter(it -> !it.getType().equals(C4Type.RELATIONSHIP))
                    .map(it -> "\"" + it.getId() + "\";")
                    .forEach(it -> dot.add(2, it));
            dot.add(1, "}");
            dot.add(0, "");
        }
        diffs.stream()
                .forEach(diff -> {
                    dot.add(1, toDot(diff, diffs, linkPrefix));
                });

        dot.add(0, "}");
        return dot.toString();
    }

    private static class Dot {
        private final StringBuilder builder = new StringBuilder();

        public String toString() {
            return builder.toString();
        }

        public void add(int indentationLevel, String line) {
            builder.append("    ".repeat(indentationLevel)).append(line).append("\n");
        }
    }

    @VisibleForTesting
    static String getDotLabel(DiffableEntity entity) {
        return "<<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\"><TR><TD>"+entity.getName()+"</TD></TR>" +
                "<TR><TD>"+entity.getType()+"</TD></TR>" +
                "<TR><TD>"+getPath(entity)+"</TD></TR>" +
                getRelatedTdds(entity)+"</TABLE>>";
    }

    static String getDotToolTip(DiffableEntity entity) {
        return "<<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\"><TR><TD>"+entity.getRelatedTo()[0] +"</TD></TR></TABLE>>";
    }

    @VisibleForTesting
    static String getDotColor(Diff diff) {
        switch (diff.getStatus()) {
            case CREATED:
                return "darkgreen";
            case DELETED:
                return "red";
            case UPDATED:
                return "blue";
            case NO_UPDATE_BUT_CHILDREN_UPDATED:
                return "blueviolet";
            case NO_UPDATE:
                return "black";
            default:
                return "black";
        }
    }

    @VisibleForTesting
    static String getUrl(Diff diff, String linkPrefix) {
        boolean shouldHaveDiagram = diff.getDescendants().stream()
                .anyMatch(it -> Set.of(C4Type.COMPONENT, C4Type.CONTAINER)
                        .contains(it.getType()));

        if (!shouldHaveDiagram) return "";

        return linkPrefix + diff.getElement().getId() + ".svg";
    }

    private static String getPath(DiffableEntity entity) {
        return entity.getEntity().getPath() == null ? "" : entity.getEntity().getPath().getPath();
    }

    private static String getRelatedTdds(DiffableEntity entity) {
        return entity.getRelatedTo() == null || entity.getRelatedTo().length == 0 ? "" : Arrays.stream(entity.getRelatedTo()).map( r -> "<TR><TD> TDD: " + r + "</TD></TR>").reduce("", String::concat);
    }

    @VisibleForTesting
    static String toDot(Diff diff, Collection<Diff> allDiffs, String linkPrefix) {
        if (diff.getElement() instanceof DiffableEntity) {
            final var entity = (DiffableEntity) diff.getElement();
            return "\"" + entity.getId() + "\" " +
                    "[label=" + getDotLabel(entity) +
                    ", color=" + getDotColor(diff) +
                    ", fontcolor=" + getDotColor(diff) +
                    ", shape=plaintext" +
                    ", URL=\"" + getUrl(diff, linkPrefix) + "\"" +
                     getTooltip(diff) +
                    "];";
        }
        final var relationship = (DiffableRelationship) diff.getElement();
        return "\"" +
                relationship.getSourceId() + "\" -> \"" + relationship.getRelationship().getWithId() +
                "\" " +
                "[label=\"" + relationship.getName() +
                "\", color=" + getDotColor(diff) +
                ", fontcolor=" + getDotColor(diff) +
                ", tooltip=\"" + getTooltip(relationship, allDiffs) + "\"" +
                ", labeltooltip=\"" + getTooltip(relationship, allDiffs) + "\"" +
                "];";
    }

    private static String getTooltip(Diff diff) {
        if (diff.getElement().getRelatedTo() == null || diff.getElement().getRelatedTo().length == 0 )
            return "";
        return " ,tooltip=\"" + Arrays.stream(diff.getElement().getRelatedTo()).collect(Collectors.joining(",")) + "\"";
    }

    private static String createText(Diff diff, String linkPrefix, DiffableEntity entity) {
        return "\"" + entity.getId() + "\" " +
                "[label=" + getDotLabel(entity) +
                ", color=" + getDotColor(diff) +
                ", fontcolor=" + getDotColor(diff) +
                ", shape=plaintext" +
                ", URL=\"" + getUrl(diff, linkPrefix) + "\"" +
                "];";
    }


    private static String createHoverText(Diff diff, String linkPrefix, DiffableEntity entity) {
        return "\"" + entity.getId() + "-TDD\" " +
                "[label=" + getDotToolTip(entity) +
                ", color=" + getDotColor(diff) +
                ", fontcolor=" + getDotColor(diff) +
                ", shape=plaintext" +
                ", class=tooltip" +
                ", URL=\"" + getUrl(diff, linkPrefix) + "\"" +
                "];";
    }

    @VisibleForTesting
    static String getTooltip(DiffableRelationship rel, Collection<Diff> allDiffs) {
        String source = allDiffs.stream()
            .filter(it -> it.getElement().getId().equals(rel.getSourceId()))
            .findAny()
            .map(it -> it.getElement().getName())
            .orElse(rel.getSourceId());
        String destination = allDiffs.stream()
            .filter(it -> it.getElement().getId().equals(rel.getDestinationId()))
            .findAny()
            .map(it -> it.getElement().getName())
            .orElse(rel.getDestinationId());
        return source + " -> " + destination;
    }
}
