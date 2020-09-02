package net.trilogy.arch.adapter.graphviz;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.parse.Parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class GraphvizInterface {
    public void render(String dotGraph, Path outputPath) throws IOException {
        final var graph = new Parser().read(dotGraph);
        File file = new File(outputPath.toAbsolutePath().toString());
        Graphviz.fromGraph(graph).render(Format.SVG).toFile(file);
    }
}
