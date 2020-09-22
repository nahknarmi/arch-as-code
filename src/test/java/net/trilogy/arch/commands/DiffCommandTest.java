package net.trilogy.arch.commands;

import net.trilogy.arch.adapter.architectureUpdate.ArchitectureUpdateReader;
import net.trilogy.arch.adapter.git.GitInterface;
import net.trilogy.arch.domain.architectureUpdate.YamlArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddComponentReference;
import net.trilogy.arch.domain.architectureUpdate.YamlTdd.TddId;
import net.trilogy.arch.domain.architectureUpdate.YamlTddContainerByComponent;
import net.trilogy.arch.domain.c4.C4Component;
import net.trilogy.arch.domain.diff.Diff;
import net.trilogy.arch.domain.diff.DiffableEntity;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static net.trilogy.arch.commands.DiffCommand.diffConnectToTdds;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class DiffCommandTest {
    @Test
    public void shouldLoadArchitectureUpdateWhenFound() throws Exception {
        final var mockFiles = mock(FilesFacade.class);
        final var mockGit = mock(GitInterface.class);

        final var mockAuReader = mock(ArchitectureUpdateReader.class);
        when(mockAuReader.loadArchitectureUpdate(any(Path.class))).thenReturn(mock(YamlArchitectureUpdate.class));

        final var diffCommand = new DiffCommand(mockFiles, mockGit, mockAuReader);
        final var diffCommandSpy = spy(diffCommand);
        //noinspection ResultOfMethodCallIgnored
        doReturn(new File("")).when(diffCommandSpy).getArchitectureUpdateDirectory();

        final var architectureUpdate = diffCommandSpy.loadArchitectureUpdate();

        assertTrue(architectureUpdate.isPresent());
    }

    @Test
    public void shouldSkipLoadingArchitectureUpdateAUParameterIsNotSet() throws Exception {
        final var mockFiles = mock(FilesFacade.class);
        final var mockGit = mock(GitInterface.class);

        final var mockAuReader = mock(ArchitectureUpdateReader.class);
        when(mockAuReader.loadArchitectureUpdate(any(Path.class))).thenReturn(mock(YamlArchitectureUpdate.class));

        final var diffCommand = new DiffCommand(mockFiles, mockGit, mockAuReader);

        final var architectureUpdate = diffCommand.loadArchitectureUpdate();

        assertTrue(architectureUpdate.isEmpty());
    }

    @Test
    public void shouldAssignTddsToComponentsInTheDiff() {
        final var componentLevelDiffs = new HashSet<Diff>();
        final var c1 = new Diff(null, new DiffableEntity(C4Component.builder()
                .id("c1")
                .name("c1")
                .build()));
        componentLevelDiffs.add(c1);
        final var c2 = new Diff(null, new DiffableEntity(C4Component.builder()
                .id("c2")
                .name("c2")
                .build()));
        componentLevelDiffs.add(c2);
        final var c3 = new Diff(null, new DiffableEntity(C4Component.builder()
                .id("c3")
                .name("c3")
                .build()));
        componentLevelDiffs.add(c3);

        final var componentTdds = new ArrayList<YamlTddContainerByComponent>();
        final var c1Tdds = new HashMap<TddId, YamlTdd>();
        c1Tdds.put(new TddId("123"), new YamlTdd("123 text", null));
        c1Tdds.put(new TddId("456"), new YamlTdd("456 text", null));
        componentTdds.add(YamlTddContainerByComponent.builder()
                .componentId(new TddComponentReference("c1"))
                .tdds(c1Tdds)
                .build());

        final var c2Tdds = new HashMap<TddId, YamlTdd>();
        c2Tdds.put(new TddId("789"), new YamlTdd("789 text", null));
        componentTdds.add(YamlTddContainerByComponent.builder()
                .componentId(new TddComponentReference("c2"))
                .tdds(c2Tdds)
                .build());

        diffConnectToTdds(componentLevelDiffs, YamlArchitectureUpdate.builder()
                .tddContainersByComponent(componentTdds)
                .build());

        assertTrue(c1.getElement().hasRelatedTdds());
        assertThat(c1.getElement().getRelatedTddsText(), hasItemInArray("123 - 123 text"));
        assertThat(c1.getElement().getRelatedTddsText(), hasItemInArray("456 - 456 text"));
        assertThat(c1.getElement().getRelatedTddsText().length, equalTo(2));

        assertTrue(c2.getElement().hasRelatedTdds());
        assertThat(c2.getElement().getRelatedTddsText(), hasItemInArray("789 - 789 text"));
        assertThat(c2.getElement().getRelatedTddsText().length, equalTo(1));

        assertFalse(c3.getElement().hasRelatedTdds());
    }
}
