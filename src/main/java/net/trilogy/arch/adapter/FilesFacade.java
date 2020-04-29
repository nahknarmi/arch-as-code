package net.trilogy.arch.adapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FilesFacade {
    public Path writeString(Path path, String toWrite) throws IOException {
        return Files.writeString(path, toWrite);
    }
}
