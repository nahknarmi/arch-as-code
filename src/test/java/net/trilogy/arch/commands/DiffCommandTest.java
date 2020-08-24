package net.trilogy.arch.commands;

import net.trilogy.arch.adapter.architectureUpdate.ArchitectureUpdateReader;
import net.trilogy.arch.adapter.git.GitInterface;
import net.trilogy.arch.adapter.graphviz.GraphvizInterface;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.Tdd;
import net.trilogy.arch.domain.architectureUpdate.TddContainerByComponent;
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
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DiffCommandTest {

    @Test
    public void shouldLoadArchitectureUpdateWhenFound() throws Exception {
        FilesFacade filesFacade = mock(FilesFacade.class);
        GitInterface gitInterface = mock(GitInterface.class);
        GraphvizInterface graphvizInterface = mock(GraphvizInterface.class);

        ArchitectureUpdateReader architectureUpdateReader = mock(ArchitectureUpdateReader.class);
        when(architectureUpdateReader.load(any(Path.class))).thenReturn(mock(ArchitectureUpdate.class));

        DiffCommand diffCommand = new DiffCommand(filesFacade, gitInterface, graphvizInterface, architectureUpdateReader);
        DiffCommand diffCommandSpy = spy(diffCommand);
        doReturn(new File("")).when(diffCommandSpy).getArchitectureUpdateDirectory();

        Optional<ArchitectureUpdate> architectureUpdate = diffCommandSpy.loadArchitectureUpdate();

        assertTrue(architectureUpdate.isPresent());
    }

    @Test
    public void shouldSkipLoadingArchitectureUpdateAUParameterIsNotSet() throws Exception {
        FilesFacade filesFacade = mock(FilesFacade.class);
        GitInterface gitInterface = mock(GitInterface.class);
        GraphvizInterface graphvizInterface = mock(GraphvizInterface.class);

        ArchitectureUpdateReader architectureUpdateReader = mock(ArchitectureUpdateReader.class);
        when(architectureUpdateReader.load(any(Path.class))).thenReturn(mock(ArchitectureUpdate.class));

        DiffCommand diffCommand = new DiffCommand(filesFacade, gitInterface, graphvizInterface, architectureUpdateReader);

        Optional<ArchitectureUpdate> architectureUpdate = diffCommand.loadArchitectureUpdate();

        assertTrue(architectureUpdate.isEmpty());
    }

    @Test
    public void shouldAssignTddsToComponentsInTheDiff() {
        FilesFacade filesFacade = mock(FilesFacade.class);
        GitInterface gitInterface = mock(GitInterface.class);
        GraphvizInterface graphvizInterface = mock(GraphvizInterface.class);

        ArchitectureUpdateReader architectureUpdateReader = mock(ArchitectureUpdateReader.class);

        DiffCommand diffCommand = new DiffCommand(filesFacade, gitInterface, graphvizInterface, architectureUpdateReader);

        HashSet<Diff> componentLevelDiffs = new HashSet<>();
        Diff c1 = new Diff(null, new DiffableEntity(C4Component.builder().id("c1").name("c1").build()));
        componentLevelDiffs.add(c1);
        Diff c2 = new Diff(null, new DiffableEntity(C4Component.builder().id("c2").name("c2").build()));
        componentLevelDiffs.add(c2);
        Diff c3 = new Diff(null, new DiffableEntity(C4Component.builder().id("c3").name("c3").build()));
        componentLevelDiffs.add(c3);

        ArrayList<TddContainerByComponent> componentTdds = new ArrayList<>();
        HashMap<Tdd.Id, Tdd> c1Tdds = new HashMap<>();
        c1Tdds.put(new Tdd.Id("123"), new Tdd("123 text", null));
        c1Tdds.put(new Tdd.Id("456"), new Tdd("456 text", null));
        componentTdds.add(TddContainerByComponent.builder().componentId(new Tdd.ComponentReference("c1")).tdds(c1Tdds).build());

        HashMap<Tdd.Id, Tdd> c2Tdds = new HashMap<>();
        c2Tdds.put(new Tdd.Id("789"), new Tdd("789 text", null));
        componentTdds.add(TddContainerByComponent.builder().componentId(new Tdd.ComponentReference("c2")).tdds(c2Tdds).build());

        diffCommand.connectToTdds(componentLevelDiffs, Optional.of(ArchitectureUpdate.builder().tddContainersByComponent(componentTdds).build()));

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
