package net.trilogy.arch.commands.architectureUpdate;

import net.trilogy.arch.adapter.git.GitInterface;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuFinalizeAndPublishCommandTest {
    @Test
    public void shouldCallAuAnnotateAndAuValidateCommands() {
        final var finalizeSpy = spy(new AuFinalizeAndPublishCommand(
                mock(FilesFacade.class),
                mock(GitInterface.class)));

        AuAnnotateCommand annotateCommand = mock(AuAnnotateCommand.class);
        when(annotateCommand.call()).thenReturn(0);
        AuValidateCommand validateCommand = mock(AuValidateCommand.class);
        when(validateCommand.call()).thenReturn(0);

        doReturn(annotateCommand).when(finalizeSpy).createAuAnnotateCommand();
        doReturn(validateCommand).when(finalizeSpy).createAuValidateCommand();
        doReturn(0).when(finalizeSpy).publishToJira();

        assertThat(finalizeSpy.call(), equalTo(0));

        verify(annotateCommand, times(1)).call();
        verify(validateCommand, times(1)).call();
    }

    @Test
    public void shouldReturnExitCodeFromAuAnnotateCommand() {
        final var finalizeSpy = spy(new AuFinalizeAndPublishCommand(
                mock(FilesFacade.class),
                mock(GitInterface.class)));

        AuAnnotateCommand annotateCommand = mock(AuAnnotateCommand.class);
        when(annotateCommand.call()).thenReturn(-11);
        AuValidateCommand validateCommand = mock(AuValidateCommand.class);
        when(validateCommand.call()).thenReturn(0);

        doReturn(annotateCommand).when(finalizeSpy).createAuAnnotateCommand();
        doReturn(validateCommand).when(finalizeSpy).createAuValidateCommand();
        doReturn(0).when(finalizeSpy).publishToJira();

        assertThat(finalizeSpy.call(), equalTo(-11));

        verify(annotateCommand, times(1)).call();
        verify(validateCommand, times(0)).call();
    }

    @Test
    public void shouldReturnExitCodeFromAuValidateCommand() {
        final var finalizeSpy = spy(new AuFinalizeAndPublishCommand(
                mock(FilesFacade.class),
                mock(GitInterface.class)));

        AuAnnotateCommand annotateCommand = mock(AuAnnotateCommand.class);
        when(annotateCommand.call()).thenReturn(0);
        AuValidateCommand validateCommand = mock(AuValidateCommand.class);
        when(validateCommand.call()).thenReturn(-12);

        doReturn(annotateCommand).when(finalizeSpy).createAuAnnotateCommand();
        doReturn(validateCommand).when(finalizeSpy).createAuValidateCommand();
        doReturn(0).when(finalizeSpy).publishToJira();

        assertThat(finalizeSpy.call(), equalTo(-12));

        verify(annotateCommand, times(1)).call();
        verify(validateCommand, times(1)).call();
    }

    @Test
    public void shouldReturnExitCodeFromAuPublishCommand() {
        final var finalizeSpy = spy(new AuFinalizeAndPublishCommand(
                mock(FilesFacade.class),
                mock(GitInterface.class)));

        AuAnnotateCommand annotateCommand = mock(AuAnnotateCommand.class);
        when(annotateCommand.call()).thenReturn(0);
        AuValidateCommand validateCommand = mock(AuValidateCommand.class);
        when(validateCommand.call()).thenReturn(0);

        doReturn(annotateCommand).when(finalizeSpy).createAuAnnotateCommand();
        doReturn(validateCommand).when(finalizeSpy).createAuValidateCommand();
        doReturn(-13).when(finalizeSpy).publishToJira();

        assertThat(finalizeSpy.call(), equalTo(-13));

        verify(annotateCommand, times(1)).call();
        verify(validateCommand, times(1)).call();
    }
}
