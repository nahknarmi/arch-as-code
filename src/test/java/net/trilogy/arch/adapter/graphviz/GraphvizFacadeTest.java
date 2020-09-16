package net.trilogy.arch.adapter.graphviz;

import org.junit.Test;

import java.nio.file.Files;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class GraphvizFacadeTest {
    @Test
    public void shouldRenderSvg() throws Exception {
        var dir = Files.createTempDirectory("aac");
        String dot = "digraph G { a -> b; }";

        GraphvizFacade.render(dot, dir.resolve("file.svg"));

        assertThat(dir.resolve("file.svg").toFile().exists(), is(true));

        final String actual = Files.readString(dir.resolve("file.svg"));
        assertThat(actual, containsString("<svg"));
        assertThat(actual, containsString("</svg>"));
        assertThat(actual, containsString("<title>a</title>"));
        assertThat(actual, containsString("<title>b</title>"));
    }
}
