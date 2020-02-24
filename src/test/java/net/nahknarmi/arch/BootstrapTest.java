package net.nahknarmi.arch;

import net.nahknarmi.arch.adapter.WorkspaceConfigLoader;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class BootstrapTest {


    @Test
    public void should_print_arch_as_code_when_no_args() {
        String[] args = new String[]{};
        int exitCode = new Bootstrap().execute(args);

        assertThat(exitCode, equalTo(0));
    }

    @Test
    public void should_fail_when_no_parameters_passed_to_initialize_command() {
        String[] args = new String[]{"init"};
        int exitCode = new Bootstrap().execute(args);

        assertThat(exitCode, equalTo(2));
    }

    @Test
    public void should_initialize_workspace_when_all_parameters_passed_to_initialize_command() {
        Long workspaceId = new WorkspaceConfigLoader().config().getWorkspaceId();

        String[] args = new String[]{"init", "-i", String.valueOf(workspaceId)};
        int exitCode = new Bootstrap().execute(args);

        assertThat(exitCode, equalTo(2));
    }
}