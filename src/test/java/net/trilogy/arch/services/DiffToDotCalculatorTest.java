package net.trilogy.arch.services;

import net.trilogy.arch.ArchitectureDataStructureHelper;
import net.trilogy.arch.domain.architectureUpdate.Tdd;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddId;
import net.trilogy.arch.domain.c4.C4Component;
import net.trilogy.arch.domain.c4.C4Path;
import net.trilogy.arch.domain.diff.Diff;
import net.trilogy.arch.domain.diff.DiffableEntity;
import net.trilogy.arch.domain.diff.DiffableRelationship;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.trilogy.arch.ArchitectureDataStructureHelper.createComponent;
import static net.trilogy.arch.ArchitectureDataStructureHelper.createContainer;
import static net.trilogy.arch.ArchitectureDataStructureHelper.createPerson;
import static net.trilogy.arch.ArchitectureDataStructureHelper.createRelationship;
import static net.trilogy.arch.ArchitectureDataStructureHelper.createSystem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DiffToDotCalculatorTest {

    @Test
    public void shouldGenerateEmptyGraph() {
        var actual = DiffToDotCalculator.toDot("title", Set.of(), null, "");
        var expected = new StringBuilder();
        appendln(expected, "digraph \"title\" {");
        appendln(expected, "    graph [rankdir=LR];");
        appendln(expected, "");
        appendln(expected, "}");

        assertThat(actual, equalTo(expected.toString()));
    }

    @Test
    public void shouldGenerateEmptyGraphWithParent() {
        var parentSystem = new Diff(
                new DiffableEntity(createSystem("parent-system")),
                null
        );
        var actual = DiffToDotCalculator.toDot("title", Set.of(), parentSystem, "");
        var expected = new StringBuilder();
        appendln(expected, "digraph \"title\" {");
        appendln(expected, "    graph [rankdir=LR];");
        appendln(expected, "");
        appendln(expected, "    subgraph \"cluster_parent-system\" {");
        appendln(expected, "        style=filled;");
        appendln(expected, "        color=grey92;");
        appendln(expected, "        label=\"system-parent-system\";");
        appendln(expected, "    }");
        appendln(expected, "");
        appendln(expected, "}");

        assertThat(actual, equalTo(expected.toString()));
    }

    @Test
    public void shouldGenerateGraph() {
        var actual = DiffToDotCalculator.toDot("title", List.of(
                new Diff(
                        new DiffableEntity(createPerson("4")),
                        null
                ),
                new Diff(
                        new DiffableEntity(createPerson("1")),
                        new DiffableEntity(createPerson("1"))
                ),
                new Diff(
                        new DiffableRelationship("1", createRelationship("10", "3")),
                        new DiffableRelationship("1", createRelationship("10", "4"))
                )
        ), null, "");

        var expected = new StringBuilder();
        appendln(expected, "digraph \"title\" {");
        appendln(expected, "    graph [rankdir=LR];");
        appendln(expected, "");
        appendln(expected, "    \"4\" [label=<<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\"><TR><TD>person-4</TD></TR><TR><TD>PERSON</TD></TR><TR><TD></TD></TR></TABLE>>, color=red, fontcolor=red, shape=plaintext, URL=\"\"];");
        appendln(expected, "    \"1\" [label=<<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\"><TR><TD>person-1</TD></TR><TR><TD>PERSON</TD></TR><TR><TD></TD></TR></TABLE>>, color=black, fontcolor=black, shape=plaintext, URL=\"\"];");
        appendln(expected, "    \"1\" -> \"4\" [label=\"d10\", color=blue, fontcolor=blue, tooltip=\"person-1 -> person-4\", labeltooltip=\"person-1 -> person-4\"];");
        appendln(expected, "}");

        assertThat(actual, equalTo(expected.toString()));
    }

    @Test
    public void shouldRenderNodesInsideParent() {
        Diff diffOwnedByParent = new Diff(new DiffableEntity(createPerson("1")), null);
        Diff diffNotOwnedByParent = new Diff(new DiffableEntity(createPerson("2")), null);
        Diff diffOfRelationship = new Diff(new DiffableRelationship("1", createRelationship("10", "2")), null);
        Diff descendentDiffToNotDisplay = new Diff(new DiffableEntity(createPerson("3")), null);
        var diffsToDisplay = List.of(
                diffOwnedByParent,
                diffNotOwnedByParent,
                diffOfRelationship
        );
        var parentSystem = new Diff(
                new DiffableEntity(createSystem("parent-system")),
                Set.of(diffOwnedByParent.getElement(), diffOfRelationship.getElement(), descendentDiffToNotDisplay.getElement()),
                null,
                null
        );
        var actual = DiffToDotCalculator.toDot("title", diffsToDisplay, parentSystem, "");
        var expected = new StringBuilder();
        appendln(expected, "digraph \"title\" {");
        appendln(expected, "    graph [rankdir=LR];");
        appendln(expected, "");
        appendln(expected, "    subgraph \"cluster_parent-system\" {");
        appendln(expected, "        style=filled;");
        appendln(expected, "        color=grey92;");
        appendln(expected, "        label=\"system-parent-system\";");
        appendln(expected, "        \"1\";");
        appendln(expected, "    }");
        appendln(expected, "");
        appendln(expected, "    \"1\" [label=<<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\"><TR><TD>person-1</TD></TR><TR><TD>PERSON</TD></TR><TR><TD></TD></TR></TABLE>>, color=red, fontcolor=red, shape=plaintext, URL=\"\"];");
        appendln(expected, "    \"2\" [label=<<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\"><TR><TD>person-2</TD></TR><TR><TD>PERSON</TD></TR><TR><TD></TD></TR></TABLE>>, color=red, fontcolor=red, shape=plaintext, URL=\"\"];");
        appendln(expected, "    \"1\" -> \"2\" [label=\"d10\", color=red, fontcolor=red, tooltip=\"person-1 -> person-2\", labeltooltip=\"person-1 -> person-2\"];");
        appendln(expected, "}");

        assertThat(actual, equalTo(expected.toString()));
    }

    @Test
    public void shouldCalculateNoUpdateColor() {
        var actual = DiffToDotCalculator.getDotColor(
                new Diff(
                        new DiffableEntity(createPerson("4")),
                        new DiffableEntity(createPerson("4"))
                )
        );

        var expected = "black";

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void shouldCalculateCreatedColor() {
        var actual = DiffToDotCalculator.getDotColor(
                new Diff(
                        null,
                        new DiffableEntity(createPerson("4"))
                )
        );

        var expected = "darkgreen";

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void shouldCalculateUpdatedColor() {
        var actual = DiffToDotCalculator.getDotColor(
                new Diff(
                        new DiffableEntity(createPerson("4")),
                        new DiffableEntity(createPerson("5"))
                )
        );

        var expected = "blue";

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void shouldCalculateNoUpdateChilderUpdatedColor() {
        var actual = DiffToDotCalculator.getDotColor(
                new Diff(
                        new DiffableEntity(createPerson("4")), Set.of(),
                        new DiffableEntity(createPerson("4")), Set.of(new DiffableEntity(createPerson("impossible-child")))
                )
        );

        var expected = "blueviolet";

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void shouldCalculateDeletedColor() {
        var actual = DiffToDotCalculator.getDotColor(
                new Diff(
                        new DiffableEntity(createPerson("4")),
                        null
                )
        );

        var expected = "red";

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void shouldGenerateRelationshipDotEntry() {
        var p4 = createPerson("4");
        var p5 = createPerson("5");
        var rel = createRelationship("r", "5");
        var diff = new Diff(new DiffableRelationship(p4, rel), null);
        var actual = DiffToDotCalculator.toDot(
                diff,
                Set.of(new Diff(new DiffableEntity(p4), null), new Diff(new DiffableEntity(p5), null), diff),
                ""
        );

        var expected = "\"4\" -> \"5\" [label=\"dr\", color=red, fontcolor=red, tooltip=\"person-4 -> person-5\", labeltooltip=\"person-4 -> person-5\"];";

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void shouldNotGenerateUrlIfNoChildren() {
        var diff = new Diff(
                new DiffableEntity(createPerson("5")),
                null
        );
        String url = DiffToDotCalculator.getUrl(diff, "prefix");
        assertThat(url, equalTo(""));
    }

    @Test
    public void shouldNotGenerateUrlIfWrongTypeOfChildren() {
        var diff = new Diff(
                new DiffableEntity(createPerson("5")),
                Set.of(
                        new DiffableEntity(createPerson("4")),
                        new DiffableEntity(createSystem("3")),
                        new DiffableRelationship("2", createRelationship("3", "4"))
                ),
                null,
                null
        );
        String url = DiffToDotCalculator.getUrl(diff, "prefix/");
        assertThat(url, equalTo(""));
    }

    @Test
    public void shouldGenerateUrlIfHasContainerChild() {
        var diff = new Diff(
                new DiffableEntity(createPerson("5")),
                Set.of(new DiffableEntity(createContainer("4", "3"))),
                null,
                null
        );
        String url = DiffToDotCalculator.getUrl(diff, "prefix/");
        assertThat(url, equalTo("prefix/5.svg"));
    }

    @Test
    public void shouldGenerateUrlIfHasComponentChild() {
        var diff = new Diff(
                new DiffableEntity(createPerson("5")),
                Set.of(new DiffableEntity(createComponent("4", "3"))),
                null,
                null
        );
        String url = DiffToDotCalculator.getUrl(diff, "prefix/");
        assertThat(url, equalTo("prefix/5.svg"));
    }

    @Test
    public void shouldCalculateTooltipForRelationships() {
        var p1Diff = new Diff(new DiffableEntity(createPerson("1")), null);
        var p2Diff = new Diff(null, new DiffableEntity(createPerson("2")));
        var rel = new DiffableRelationship("1", createRelationship("r", "2"));
        var relDiff = new Diff(rel, null);

        String tooltip = DiffToDotCalculator.getRelationshipTooltip(rel, Set.of(p1Diff, p2Diff, relDiff));

        assertThat(tooltip, equalTo("person-1 -> person-2"));
    }

    @Test
    public void shouldGenerateEntityDotEntry() {
        var person = createPerson("4");
        person.setPath(C4Path.path("@person-4"));
        var diff = new Diff(
                new DiffableEntity(person),
                new DiffableEntity(person)
        );
        var actual = DiffToDotCalculator.toDot(
                diff,
                Set.of(diff),
                "assets"
        );

        var expected = "\"4\" [label=<<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\"><TR><TD>person-4</TD></TR><TR><TD>PERSON</TD></TR><TR><TD>@person-4</TD></TR></TABLE>>, color=black, fontcolor=black, shape=plaintext, URL=\"\"];";

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void shouldGenerateComponentWithTddTooltipWhenComponentHasTdds() {
        C4Component component = createComponent("4", "3");
        DiffableEntity after = new DiffableEntity(component);
        after.setRelatedTdds(Map.of(new TddId("TDD 1.1"), new Tdd("Some text", null)));

        var diff = new Diff(null, after);

        var actual = DiffToDotCalculator.toDot(diff, Set.of(diff), "assets/");

        var expected = "\"4\" [label=<<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\"><TR><TD>component-4</TD></TR><TR><TD>COMPONENT</TD></TR><TR><TD></TD></TR></TABLE>>, color=darkgreen, fontcolor=darkgreen, shape=plaintext, URL=\"assets/4.svg\" ,tooltip=\"TDD 1.1 - Some text\"];";

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void shouldGenerateComponentWithTddTuncatedTooltipWhenComponentHasTddsWithLongText() {
        C4Component component = createComponent("4", "3");
        DiffableEntity after = new DiffableEntity(component);
        after.setRelatedTdds(Map.of(new TddId("TDD 1.1"),
                new Tdd("Some text for this tdd that is longer than 50 character and should be truncated with added elipses.", null)));

        var diff = new Diff(null, after);

        var actual = DiffToDotCalculator.toDot(
                diff,
                Set.of(diff),
                "assets"
        );

        var expected = "\"4\" [label=<<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\"><TR><TD>component-4</TD></TR><TR><TD>COMPONENT</TD></TR><TR><TD></TD></TR></TABLE>>, color=darkgreen, fontcolor=darkgreen, shape=plaintext, URL=\"assets/4.svg\" ,tooltip=\"TDD 1.1 - Some text for this tdd that is longer th...\"];";

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void shouldRenderRelatedTdds() {
        C4Component component = createComponent("4", "3");
        DiffableEntity after = new DiffableEntity(component);
        after.setRelatedTdds(Map.of(new TddId("TDD 1.1"), new Tdd("Some text", null)));

        var diff = new Diff(
                null,
                after
        );

        var actual = DiffToDotCalculator.toDot("Tdds", diff);

        var expected = String.join("\n",
                "digraph \"Tdds\" {",
                "    graph [rankdir=LR];",
                "",
                "    \"tdds\" [label=<<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\"><TR><TD><TABLE CELLBORDER=\"0\" CELLSPACING=\"0\"><TR><TD align=\"text\">TDD 1.1 - Some text<br align=\"left\" /></TD></TR></TABLE></TD></TR></TABLE>>, color=black, fontcolor=black, shape=plaintext];",
                "    ",
                "}");

        assertThat(actual.trim(), equalTo(expected));
    }

    private void appendln(StringBuilder builder, String line) {
        builder.append(line).append("\n");
    }
}
