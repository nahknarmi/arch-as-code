package net.nahknarmi.arch;

import net.nahknarmi.arch.adapter.WorkspaceConfig;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

import static net.nahknarmi.arch.adapter.Credentials.config;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class UserJourneyTest {
    private WorkspaceConfig config;
    private String workspaceRoot;
    private File exportedWorkspacePath;

    @Before
    public void setUp() throws Exception {
        config = config();
        workspaceRoot = Files.createTempDirectory("arch-as-code").toAbsolutePath().toString();
        exportedWorkspacePath = new File(getClass().getResource("/structurizr/Think3-Sococo.c4model.json").getPath());
    }

    @Test
    public void prints_arch_as_code_when_no_args() {
        int exitCode = parent();

        assertThat(exitCode, equalTo(0));
    }

    @Test
    public void prints_version() {
        int exitCode = version();

        assertThat(exitCode, equalTo(0));
    }

    @Test
    public void prints_help() {
        int exitCode = help();

        assertThat(exitCode, equalTo(0));
    }

    @Test
    public void fails_when_no_parameters_passed_to_initialize_command() {
        int exitCode = new Bootstrap().execute(new String[]{"init"});

        assertThat(exitCode, equalTo(2));
    }

    @Test
    public void fails_when_options_passed_but_parameter_is_not_passed_to_initialize_command() {
        int exitCode = new Bootstrap()
                .execute(new String[]{
                        "init",
                        "-i", String.valueOf(config.getWorkspaceId()),
                        "-k", config.getApiKey(),
                        "-s", config.getApiSecret()
                });

        assertThat(exitCode, equalTo(2));
    }

    @Test
    public void initializes_workspace_when_all_parameters_and_options_passed_to_initialize_command() {
        int exitCode = init();

        assertThat(exitCode, equalTo(0));
    }

    @Test
    public void fails_when_workspace_path_not_passed_to_validate_command() {
        init();

        int exitCode = new Bootstrap().execute(new String[]{"validate"});

        assertThat(exitCode, equalTo(2));
    }

    @Test
    public void validates_workspace_when_workspace_path_passed_to_validate_command() {
        init();

        int exitCode = validate();

        assertThat(exitCode, equalTo(0));
    }

    @Test
    public void should_fail_when_workspace_path_not_passed_to_publish_command() {
        init();
        validate();

        int exitCode = new Bootstrap().execute(new String[]{"publish"});

        assertThat(exitCode, equalTo(2));
    }

    @Test
    public void publishes_workspace_when_workspace_path_passed_to_validate_command() {
        init();
        validate();

        int exitCode = publish();

        assertThat(exitCode, equalTo(0));
    }

    @Test
    public void fails_when_exported_workspace_path_not_passed_to_import_command() {
        int exitCode = new Bootstrap().execute(new String[]{"import"});

        assertThat(exitCode, equalTo(2));
    }

    @Test
    public void imports_exported_workspace_when_workspace_path_passed_to_import_command() {
        int exitCode = importWorkspace();

        assertThat(exitCode, equalTo(0));
    }

    private int publish() {
        return new Bootstrap().execute(new String[]{
                "publish",
                workspaceRoot
        });
    }

    private int validate() {
        return new Bootstrap().execute(new String[]{
                "validate",
                workspaceRoot
        });
    }

    private int init() {
        return new Bootstrap().execute(new String[]{
                "init",
                "-i", String.valueOf(config.getWorkspaceId()),
                "-k", config.getApiKey(),
                "-s", config.getApiSecret(),
                workspaceRoot
        });
    }

    private int version() {
        return new Bootstrap().execute(new String[]{"--version"});
    }

    private int help() {
        return new Bootstrap().execute(new String[]{"--help"});
    }

    private int parent() {
        return new Bootstrap().execute(new String[]{});
    }

    private int importWorkspace() {
        return new Bootstrap().execute(new String[]{"import", exportedWorkspacePath.getAbsolutePath()});
    }
}