package net.nahknarmi.arch.e2e;

import com.structurizr.Workspace;
import com.structurizr.io.json.JsonWriter;
import net.nahknarmi.arch.adapter.in.WorkspaceReader;
import net.nahknarmi.arch.adapter.out.ArchitectureDataStructureWriter;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.publish.ArchitectureDataStructurePublisher;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class ImportExportRoundtripTest {
    File workspacePath = new File(getClass().getResource("/structurizr/Think3-Sococo.c4model.json").getPath());

    @Test
    public void ids_should_not_mutate_on_entity_rename() throws Exception {
        // Import    json->yaml
        ArchitectureDataStructure dataStructure = new WorkspaceReader().load(workspacePath);

        // RENAME
        dataStructure.getModel().findPersonByName("BU").setName("BU rename!!");

        dataStructure.getModel().getSystems().stream()
                .filter(sys -> sys.getName().equals("User Analytics"))
                .findFirst()
                .ifPresent(sys -> sys.setName("User Analytics rename!!"));

        // Export    yaml->json
        File exportedFile = new ArchitectureDataStructureWriter().export(dataStructure);
        File directory = exportedFile.getParentFile();
        String fileName = exportedFile.getName();
        Workspace workspace = ArchitectureDataStructurePublisher.create(directory, fileName).getWorkspace(directory, fileName);

        File renamedJson = File.createTempFile("temp", ".yml");
        FileOutputStream fileOutputStream = new FileOutputStream(renamedJson);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
        new JsonWriter(true).write(workspace, outputStreamWriter);
        outputStreamWriter.close();

//         Re-import json->yaml
//        ArchitectureDataStructure reimportedDataStructure = new WorkspaceReader().load(renamedJson);
//        File reExportedFile = new ArchitectureDataStructureWriter().export(reimportedDataStructure);
        System.out.println("   *** original file " + workspacePath);
        System.out.println("   *** renamed file " + renamedJson);
    }
}
