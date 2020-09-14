package net.trilogy.arch.adapter.jira;

import net.trilogy.arch.domain.architectureUpdate.Tdd;
import net.trilogy.arch.domain.architectureUpdate.Tdd.TddId;
import net.trilogy.arch.domain.architectureUpdate.TddContent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.util.Optional;

import static net.trilogy.arch.adapter.jira.JiraStory.JiraTdd;
import static org.hamcrest.Matchers.equalTo;

public class JiraTddTest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();

    @Test
    public void shouldReturnInlinedTddContent() {
        JiraTdd tdd = new JiraTdd(
                new TddId("TDD 1.1"),
                new Tdd("text", null),
                "c4://path",
                null
        );

        collector.checkThat(tdd.getText(), equalTo("text"));
        collector.checkThat(tdd.hasTddContent(), equalTo(false));
    }

    @Test
    public void shouldReturnFileTddContent() {
        JiraTdd tdd = new JiraTdd(
                new TddId("TDD 1.1"),
                new Tdd(null, "TDD 1.1 : Component-10.md"),
                "c4://path",
                new TddContent("#Content\nTdd Content", "TDD 1.1 : Component-10.md")
        );

        collector.checkThat(tdd.getText(), equalTo("#Content\nTdd Content"));
        collector.checkThat(tdd.hasTddContent(), equalTo(true));
    }

    @Test
    public void shouldReturnFileTddContentIfTddOmitsFile() {
        JiraTdd tdd = new JiraTdd(
                new TddId("TDD 1.1"),
                new Tdd("ignored text", null),
                "c4://path",
                new TddContent("#Content\nTdd Content", "TDD 1.1 : Component-10.md")
        );

        collector.checkThat(tdd.getText(), equalTo("#Content\nTdd Content"));
    }

    @Test
    public void shouldReturnFileTddContentIfBothPresent() {
        JiraTdd tdd = new JiraTdd(
                new TddId("TDD 1.1"),
                new Tdd("ignored text", "TDD 1.1 : Component-10.md"),
                "c4://path",
                new TddContent("#Content\nTdd Content", "TDD 1.1 : Component-10.md")
        );

        collector.checkThat(tdd.getText(), equalTo("#Content\nTdd Content"));
    }

    @Test
    public void shouldConstructJiraTddFromAuTddContents() {
        final var correctContent = Optional.of(new TddContent("correct content", "TDD 2.0 : Component-10.md"));
        JiraTdd tdd = JiraTdd.constructFrom(
                new TddId("TDD 2.0"),
                new Tdd("ignored content", "TDD 2.0 : Component-10.md"),
                "c4://path",
                        correctContent

        );

        collector.checkThat(tdd.getId(), equalTo("TDD 2.0"));
        collector.checkThat(tdd.getComponentPath(), equalTo("c4://path"));
        collector.checkThat(tdd.hasTddContent(), equalTo(true));
        collector.checkThat(tdd.getTddContent(), equalTo(correctContent));
        collector.checkThat(tdd.getText(), equalTo("correct content"));
    }

    @Test
    public void shouldConstructJiraTddFromAuTddContentsEvenIfFileOmitted() {
        final var correctContent =  Optional.of(new TddContent("correct content", "TDD 2.0 : Component-10.md"));
        JiraTdd tdd = JiraTdd.constructFrom(
                new TddId("TDD 2.0"),
                new Tdd("ignored content", null),
                "10",
                correctContent);

        collector.checkThat(tdd.getId(), equalTo("TDD 2.0"));
        collector.checkThat(tdd.getComponentPath(), equalTo("10"));
        collector.checkThat(tdd.hasTddContent(), equalTo(true));
        collector.checkThat(tdd.getTddContent(), equalTo(correctContent));
        collector.checkThat(tdd.getText(), equalTo("correct content"));
    }

    @Test
    public void shouldConstructJiraTddFromEmptyAuTddContents() {
        JiraTdd tdd = JiraTdd.constructFrom(
                new TddId("TDD 2.0"),
                new Tdd("text", null),
                "10",
                Optional.empty()
        );

        collector.checkThat(tdd.getId(), equalTo("TDD 2.0"));
        collector.checkThat(tdd.getComponentPath(), equalTo("10"));
        collector.checkThat(tdd.hasTddContent(), equalTo(false));
        collector.checkThat(tdd.getTddContent(), equalTo(null));
        collector.checkThat(tdd.getText(), equalTo("text"));
    }
}
