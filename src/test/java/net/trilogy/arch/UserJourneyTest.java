package net.trilogy.arch;

import net.trilogy.arch.adapter.structurizr.Credentials;
import net.trilogy.arch.adapter.structurizr.WorkspaceConfig;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

import static net.trilogy.arch.TestHelper.execute;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class UserJourneyTest {
    private WorkspaceConfig config;
    private String workspaceRoot;
    private File exportedWorkspacePath;

    @Before
    public void setUp() throws Exception {
        config = Credentials.config();
        workspaceRoot = Files.createTempDirectory("arch-as-code").toAbsolutePath().toString();
        exportedWorkspacePath = new File(getClass().getResource("/structurizr/Think3-Sococo.c4model.json").getPath());
    }

    @Test
    public void prints_arch_as_code_when_no_args() throws Exception {
        int exitCode = execute();

        assertThat(exitCode, equalTo(0));
    }

    @Test
    public void prints_version() throws Exception {
        int exitCode = execute("--version");

        assertThat(exitCode, equalTo(0));
    }

    @Test
    public void prints_help() throws Exception {
        int exitCode = execute("--help");

        assertThat(exitCode, equalTo(0));
    }

    @Test
    public void fails_when_no_parameters_passed_to_initialize_command() throws Exception {
        int exitCode = execute("init");

        assertThat(exitCode, equalTo(2));
    }

    @Test
    public void fails_when_options_passed_but_parameter_is_not_passed_to_initialize_command() throws Exception {
        int exitCode = execute("init",
                "-i", String.valueOf(config.getWorkspaceId()),
                "-k", config.getApiKey(),
                "-s", config.getApiSecret());

        assertThat(exitCode, equalTo(2));
    }

    @Test
    public void initializes_workspace_when_all_parameters_and_options_passed_to_initialize_command() throws Exception {
        int exitCode = init();

        assertThat(exitCode, equalTo(0));
    }

    @Test
    public void fails_when_workspace_path_not_passed_to_validate_command() throws Exception {
        init();

        int exitCode = execute("validate");

        assertThat(exitCode, equalTo(2));
    }

    @Test
    public void validates_workspace_when_workspace_path_passed_to_validate_command() throws Exception {
        init();

        int exitCode = execute("validate", workspaceRoot);

        assertThat(exitCode, equalTo(0));
    }

    @Test
    public void should_fail_when_workspace_path_not_passed_to_publish_command() throws Exception {
        init();
        execute("validate", workspaceRoot);

        int exitCode = execute("publish");

        assertThat(exitCode, equalTo(2));
    }

    @Test
    public void publishes_workspace_when_workspace_path_passed_to_validate_command() throws Exception {
        init();
        int exitCode = importWorkspace();
        assertThat(exitCode, equalTo(0));

        execute("validate", workspaceRoot);

        exitCode = execute("publish", workspaceRoot);

        assertThat(exitCode, equalTo(0));
    }

    @Test
    public void fails_when_exported_workspace_path_not_passed_to_import_command() throws Exception {
        int exitCode = execute("import");

        assertThat(exitCode, equalTo(2));
    }

    @Test
    public void imports_exported_workspace_when_workspace_path_passed_to_import_command() throws Exception {
        int exitCode = importWorkspace();

        assertThat(exitCode, equalTo(0));
    }

    private int init() throws Exception {
        return execute("init",
                "-i", String.valueOf(config.getWorkspaceId()),
                "-k", config.getApiKey(),
                "-s", config.getApiSecret(),
                workspaceRoot);
    }

    private int importWorkspace() throws Exception {
        return execute("import", exportedWorkspacePath.getAbsolutePath(), workspaceRoot);
    }
}
