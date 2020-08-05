package net.trilogy.arch.e2e.architectureUpdate;

import net.trilogy.arch.TestHelper;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.trilogy.arch.TestHelper.execute;
import static org.hamcrest.Matchers.*;

public class AuValidateCommandTest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();

    private File rootDir;
    private Path auDir;
    private Git git;

    final PrintStream originalOut = System.out;
    final PrintStream originalErr = System.err;
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @Before
    public void setUp() throws Exception {
        out.reset();
        err.reset();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
        var buildDir = new File(getClass().getResource(TestHelper.ROOT_PATH_TO_TEST_AU_VALIDATION_E2E).getPath());

        rootDir = buildDir.toPath().resolve("git").toFile();
        auDir = rootDir.toPath().resolve("architecture-updates/");

        FileUtils.copyDirectory(buildDir, rootDir);
        git = Git.init().setDirectory(rootDir).call();

        setUpRealisticGitRepository();
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(rootDir);
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    private void setUpRealisticGitRepository() throws Exception {
        Files.move(
                rootDir.toPath().resolve("master-branch-product-architecture.yml"),
                rootDir.toPath().resolve("product-architecture.yml")
        );
        git.add().addFilepattern("product-architecture.yml").call();
        git.commit().setMessage("add architecture to master").call();

        git.add().addFilepattern("architecture-updates").call();
        git.commit().setMessage("add AU yamls").call();

        git.checkout().setCreateBranch(true).setName("au").call();
        Files.delete(rootDir.toPath().resolve("product-architecture.yml"));
        Files.move(
                rootDir.toPath().resolve("au-branch-product-architecture.yml"),
                rootDir.toPath().resolve("product-architecture.yml")
        );
        git.add().addFilepattern("product-architecture.yml").call();
        git.commit().setMessage("change architecture in au").call();
    }

    @Test
    public void shouldBeFullyValid() {
        var auPath = auDir.resolve("blank/").toString();

        Integer status = execute("au", "validate", "-b", "master", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, equalTo(0));
        collector.checkThat(out.toString(), containsString("Success, no errors found."));
        collector.checkThat(err.toString(), equalTo(""));
    }

    @Test
    public void shouldBeTDDValid() {
        var auPath = auDir.resolve("invalid-capabilities/").toString();

        Integer status = execute("architecture-update", "validate", "-b", "master", "-t", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, equalTo(0));
        collector.checkThat(out.toString(), containsString("Success, no errors found."));
        collector.checkThat(err.toString(), equalTo(""));
    }

    @Test
    public void shouldBeStoryValid() {
        var auPath = auDir.resolve("invalid-tdds/").toString();

        Integer status = execute("architecture-update", "validate", "-b", "master", "-s", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, equalTo(0));
        collector.checkThat(out.toString(), containsString("Success, no errors found."));
        collector.checkThat(err.toString(), equalTo(""));
    }

    @Test
    public void shouldPresentErrorsNicely() {
        var auPath = auDir.resolve("both-invalid/").toString();

        Integer status = execute("au", "validate", "-b", "master", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(
                err.toString(),
                equalTo("" +
                        "Decision Missing TDD:\n" +
                        "    Decision \"[SAMPLE-DECISION-ID]\" must have at least one TDD reference.\n" +
                        "Invalid Component Reference:\n" +
                        "    Component id \"[INVALID-COMPONENT-ID]\" does not exist.\n" +
                        "Story Missing TDD:\n" +
                        "    Story \"[SAMPLE FEATURE STORY TITLE]\" must have at least one TDD reference.\n" +
                        "Missing Capability:\n" +
                        "    TDD \"[SAMPLE-TDD-ID]\" needs to be referenced in a story.\n" +
                        ""
                )
        );
    }

    @Test
    public void shouldBeFullyInvalid() {
        var auPath = auDir.resolve("both-invalid/").toString();

        Integer status = execute("au", "validate", "-b", "master", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(err.toString(),
                containsString("Decision \"[SAMPLE-DECISION-ID]\" must have at least one TDD reference."));
        collector.checkThat(err.toString(),
                containsString("TDD \"[SAMPLE-TDD-ID]\" needs to be referenced in a story."));
        collector.checkThat(err.toString(),
                containsString("Component id \"[INVALID-COMPONENT-ID]\" does not exist."));
    }

    @Test
    public void shouldBeTddInvalid() {
        var auPath = auDir.resolve("both-invalid/").toString();

        Integer status = execute("au", "validate", "-b", "master", "--TDDs", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(err.toString(),
                containsString("Decision \"[SAMPLE-DECISION-ID]\" must have at least one TDD reference."));
        collector.checkThat(err.toString(),
                not(containsString("TDD \"[SAMPLE-TDD-ID]\" needs to be referenced in a story.")));
        collector.checkThat(err.toString(),
                containsString("Component id \"[INVALID-COMPONENT-ID]\" does not exist."));
    }

    @Test
    public void shouldBeStoryInvalid() {
        var auPath = auDir.resolve("both-invalid/").toString();

        Integer status = execute("au", "validate", "-b", "master", "--stories", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(err.toString(),
                not(containsString("Component id \"[INVALID-COMPONENT-ID]\" does not exist.")));
        collector.checkThat(err.toString(),
                not(containsString("Decision \"[SAMPLE-DECISION-ID]\" must have at least one TDD reference.")));
        collector.checkThat(err.toString(),
                containsString("TDD \"[SAMPLE-TDD-ID]\" needs to be referenced in a story."));
    }

    @Test
    public void shouldFindErrorsAcrossGitBranches() {
        var auPath = auDir.resolve("invalid-deleted-component/").toString();

        Integer status = execute("architecture-update", "validate", "-b", "master", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(err.toString(), containsString("Deleted component id \"deleted-component-invalid\" is invalid. (Checked architecture in \"master\" branch.)"));
    }

    @Test
    public void shouldHandleIfGitReaderFails() {
        var auPath = auDir.resolve("architecture-updates/blank/").toString();

        Integer status = execute("architecture-update", "validate", "-b", "invalid", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(err.toString(), containsString("Unable to load 'invalid' branch architecture\nError thrown: net.trilogy.arch.adapter.git.GitInterface$BranchNotFoundException"));
    }

    @Test
    public void shouldHandleIfUnableToLoadArchitecture() {
        var auPath = auDir.resolve("architecture-updates/blank/").toString();

        Integer status = execute("architecture-update", "validate", "-b", "master", auPath, rootDir.getAbsolutePath() + "invalid");

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(err.toString(), containsString("Error thrown: java.nio.file.NoSuchFileException"));
    }

    @Test
    public void shouldFindAUStructureErrors() {
        var auPath = auDir.resolve("invalid-structure/").toString();

        Integer status = execute("au", "validate", "-b", "master", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(out.toString(), equalTo(""));

        collector.checkThat(
                err.toString(),
                containsString("Unable to load architecture update file\nError thrown: com.fasterxml")
        );
    }

    @Test
    public void shouldFindAUSchemaErrors() {
        var auPath = auDir.resolve("invalid-schema/").toString();

        Integer status = execute("au", "validate", "-b", "master", auPath, rootDir.getAbsolutePath());

        collector.checkThat(status, not(equalTo(0)));
        collector.checkThat(out.toString(), equalTo(""));
        collector.checkThat(
                err.toString(),
                containsString("Unrecognized field \"notext\"")
        );
    }
}
