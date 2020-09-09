package net.trilogy.arch.services;

import com.google.common.annotations.VisibleForTesting;
import net.trilogy.arch.domain.c4.C4Type;
import net.trilogy.arch.domain.diff.Diff;
import net.trilogy.arch.domain.diff.Diffable;
import net.trilogy.arch.domain.diff.DiffableEntity;
import net.trilogy.arch.domain.diff.DiffableRelationship;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

public class DiffToDotCalculator {
    public static final int MAX_TOOLTIP_TEXT_SIZE = 50;

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
        diffs
                .forEach(diff -> dot.add(1, toDot(diff, diffs, linkPrefix)));

        dot.add(0, "}");
        return dot.toString();
    }

    public static String toDot(String title, Diff component) {
        final var dot = new Dot();
        dot.add(0, "digraph \"" + title + "\" {");
        dot.add(1, "graph [rankdir=LR];");
        dot.add(0, "");

        dot.add(1,
                "\"tdds\" " +
                        "[label=" + getDotTddAsTable(component.getElement()) +
                        ", color=black" +
                        ", fontcolor=black" +
                        ", shape=plaintext" +
                        "];");

        dot.add(1, "");
        dot.add(0, "}");

        return dot.toString();
    }

    @VisibleForTesting
    static String getDotLabel(DiffableEntity entity) {
        return "<<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\"><TR><TD>" + entity.getName() + "</TD></TR>" +
                "<TR><TD>" + entity.getType() + "</TD></TR>" +
                "<TR><TD>" + getPath(entity) + "</TD></TR></TABLE>>";
    }

    static String getDotTddAsTable(Diffable entity) {
        return "<<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\">" + stream(entity.getRelatedTddsText()).map(tdd -> "<TR><TD><TABLE CELLBORDER=\"0\" CELLSPACING=\"0\">" + stream(tdd.split("\\n")).map(s -> "<TR><TD align=\"text\">" + s + "<br align=\"left\" /></TD></TR>").collect(joining()) + "</TABLE></TD></TR>").collect(joining()) + "</TABLE>>";
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
                // Java does not recognize when branches are exhausted
                throw new IllegalStateException("Unknown color diff: " + diff.getStatus());
        }
    }

    @VisibleForTesting
    static String getUrl(Diff diff, String linkPrefix) {
        boolean shouldHaveDiagram = diff.getElement().hasRelatedTdds() || diff.getDescendants().stream()
                .anyMatch(it -> Set.of(C4Type.COMPONENT, C4Type.CONTAINER)
                        .contains(it.getType()));

        if (!shouldHaveDiagram) return "";

        return (linkPrefix.isEmpty() || linkPrefix.endsWith("/") ? linkPrefix : linkPrefix + "/") + diff.getElement().getId() + ".svg";
    }

    private static String getPath(DiffableEntity entity) {
        return entity.getEntity().getPath() == null ? "" : entity.getEntity().getPath().getPath();
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
                    getRelatedTddsTooltip(diff) +
                    "];";
        }
        final var relationship = (DiffableRelationship) diff.getElement();
        return "\"" +
                relationship.getSourceId() + "\" -> \"" + relationship.getRelationship().getWithId() +
                "\" " +
                "[label=\"" + relationship.getName() +
                "\", color=" + getDotColor(diff) +
                ", fontcolor=" + getDotColor(diff) +
                ", tooltip=\"" + getRelationshipTooltip(relationship, allDiffs) + "\"" +
                ", labeltooltip=\"" + getRelationshipTooltip(relationship, allDiffs) + "\"" +
                "];";
    }

    private static String getRelatedTddsTooltip(Diff diff) {
        if (diff.getElement().hasRelatedTdds()) {
            return " ,tooltip=\"" + stream(diff.getElement().getRelatedTddsText())
                    .map(DiffToDotCalculator::truncateLongText)
                    .collect(joining("\n")) + "\"";
        }
        return "";
    }

    private static String truncateLongText(String text) {
        return text.length() < MAX_TOOLTIP_TEXT_SIZE
                ? text
                : text.substring(0, MAX_TOOLTIP_TEXT_SIZE) + "...";
    }

    @VisibleForTesting
    static String getRelationshipTooltip(DiffableRelationship rel, Collection<Diff> allDiffs) {
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

    private static class Dot {
        private final StringBuilder builder = new StringBuilder();

        public String toString() {
            return builder.toString();
        }

        public void add(int indentationLevel, String line) {
            builder.append("    ".repeat(indentationLevel)).append(line).append("\n");
        }
    }
}
