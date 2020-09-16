package net.trilogy.arch.adapter.graphviz;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.parse.Parser;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@UtilityClass
public class GraphvizFacade {
    public static void render(String dotGraph, Path outputPath) throws IOException {
        final var graph = new Parser().read(dotGraph);
        final var file = new File(outputPath.toAbsolutePath().toString());

        Graphviz.fromGraph(graph).render(Format.SVG).toFile(file);
    }
}
