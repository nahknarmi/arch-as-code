package net.trilogy.arch.e2e;

import net.trilogy.arch.Application;
import net.trilogy.arch.TestHelper;
import net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper;
import net.trilogy.arch.adapter.git.GitInterface;
import net.trilogy.arch.adapter.graphviz.GraphvizInterface;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import net.trilogy.arch.facade.FilesFacade;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;

import static net.trilogy.arch.TestHelper.execute;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DiffCommandE2ETest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();

    final PrintStream originalOut = System.out;
    final PrintStream originalErr = System.err;
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private Application app;
    private File rootDir;
    private GitInterface mockedGitInterface;
    private File outputDirParent;

    @Before
    public void setUp() throws Exception {
        out.reset();
        err.reset();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));

        rootDir = new File(getClass().getResource(TestHelper.ROOT_PATH_TO_TEST_DIFF_COMMAND).getPath());
        outputDirParent = Files.createTempDirectory("aac").toFile();

        mockedGitInterface = mock(GitInterface.class);
        app = Application.builder().gitInterface(mockedGitInterface).build();
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.forceDelete(outputDirParent);
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void shouldFailIfCannotCreateDirectory() throws Exception {
        // GIVEN
        mockGitInterface();

        // WHEN
        final int status = execute(app, "diff -b master " + rootDir.getAbsolutePath() + " -o "
                + outputDirParent.getAbsolutePath());

        // THEN
        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(err.toString(), containsString("Unable to create output directory"));
        collector.checkThat(out.toString(), equalTo(""));
    }

    @Test
    public void shouldHaveRightOutput() throws Exception {
        // GIVEN
        mockGitInterface();
        final var outputPath = outputDirParent.toPath().resolve("ourOutputDir").toAbsolutePath();

        // WHEN
        final Integer status = execute(app,
                "diff -b master " + rootDir.getAbsolutePath() + " -o " + outputPath.toString());

        // THEN
        collector.checkThat(out.toString(),
                equalTo("SVG files created in " + outputPath.toString() + "\n"));
        collector.checkThat(err.toString(), equalTo(""));
        collector.checkThat(status, equalTo(0));
    }

    @Test
    public void shouldCreateSystemLevelDiffSvg() throws Exception {
        // GIVEN
        mockGitInterface();
        final var outputPath = outputDirParent.toPath().resolve("ourOutputDir").toAbsolutePath();

        // WHEN
        execute(app, "diff -b master " + rootDir.getAbsolutePath() + " -o " + outputPath.toString());

        // THEN
        final var svgContent = Files.readString(outputPath.resolve("system-context-diagram.svg"));
        collector.checkThat(svgContent, containsString("<title>2</title>")); // person
        collector.checkThat(svgContent, containsString("<title>6</title>")); // system
        collector.checkThat(svgContent, not(containsString("<title>13</title>"))); // container
        collector.checkThat(svgContent, not(containsString("<title>14</title>"))); // component
        collector.checkThat(svgContent, containsString("<a xlink:href=\"assets/9.svg\"")); // system url
    }

    @Test
    public void shouldCreateContainerLevelDiffSvg() throws Exception {
        // GIVEN
        mockGitInterface();
        final var outputPath = outputDirParent.toPath().resolve("ourOutputDir").toAbsolutePath();

        // WHEN
        execute(app, "diff -b master " + rootDir.getAbsolutePath() + " -o " + outputPath.toString());

        // THEN
        collector.checkThat(Files.exists(outputPath.resolve("assets/9.svg")), is(true));

        final var svgContent = Files.readString(outputPath.resolve("assets/9.svg"));
        collector.checkThat(svgContent, containsString("cluster_9"));
        collector.checkThat(svgContent, containsString("<title>13</title>"));
        collector.checkThat(svgContent, containsString("<title>12</title>"));
        collector.checkThat(svgContent, containsString("<title>11</title>"));
        collector.checkThat(svgContent, containsString("<title>10</title>"));
        collector.checkThat(svgContent, containsString("<a xlink:href=\"13.svg\"")); // container url
    }

    @Test
    public void shouldCreateComponentLevelDiffSvg() throws Exception {
        // GIVEN
        mockGitInterface();
        final var outputPath = outputDirParent.toPath().resolve("ourOutputDir").toAbsolutePath();

        // WHEN
        execute(app, "diff -b master " + rootDir.getAbsolutePath() + " -o " + outputPath.toString());

        // THEN
        collector.checkThat(Files.exists(outputPath.resolve("assets/13.svg")), is(true));

        final var svgContent = Files.readString(outputPath.resolve("assets/13.svg"));
        collector.checkThat(svgContent, containsString("cluster_13"));
        collector.checkThat(svgContent, containsString("<title>16</title>"));
        collector.checkThat(svgContent, containsString("<title>14</title>"));
        collector.checkThat(svgContent, containsString("<title>38</title>"));
        collector.checkThat(svgContent, containsString("<title>15</title>"));
        collector.checkThat(svgContent, containsString("<title>[SAMPLE&#45;COMPONENT&#45;ID]</title>"));
    }

    @Test
    public void shouldCreateRightNumberOfDiagrams() throws Exception {
        // GIVEN
        mockGitInterface();
        final var outputPath = outputDirParent.toPath().resolve("ourOutputDir").toAbsolutePath();

        // WHEN
        execute(app, "diff -b master " + rootDir.getAbsolutePath() + " -o " + outputPath.toString());

        // THEN
        collector.checkThat(Files.list(outputPath.resolve("assets")).filter(it -> it.getFileName().toString().contains(".svg")).count(), equalTo(2L));
    }

    @Test
    public void shouldHandleIfGraphvizFails() throws Exception {
        // GIVEN
        mockGitInterface();

        final var mockedGraphvizInterface = mock(GraphvizInterface.class);
        doThrow(new RuntimeException("BOOM!")).when(mockedGraphvizInterface).render(any(), any());

        var app = Application.builder()
                .gitInterface(mockedGitInterface)
                .graphvizInterface(mockedGraphvizInterface)
                .build();

        // WHEN
        final var outputPath = outputDirParent.toPath().resolve("ourOutputDir").toAbsolutePath();
        final Integer status = execute(app,
                "diff -b master " + rootDir.getAbsolutePath() + " -o " + outputPath.toString());

        // THEN
        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(err.toString(), containsString("Unable to render SVG"));
        collector.checkThat(out.toString(), equalTo(""));
    }

    private void mockGitInterface() throws IOException, GitAPIException, GitInterface.BranchNotFoundException {
        final String architectureAsString = new FilesFacade()
                .readString(rootDir.toPath().resolve("product-architecture.yml"))
                .replaceAll("id: \"16\"", "id: \"116\"");
        final ArchitectureDataStructure dataStructure = new ArchitectureDataStructureObjectMapper()
                .readValue(architectureAsString);
        when(mockedGitInterface.load("master",
                rootDir.toPath().resolve("product-architecture.yml"))).thenReturn(dataStructure);
    }
}
