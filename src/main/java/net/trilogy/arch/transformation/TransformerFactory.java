package net.trilogy.arch.transformation;

import com.google.common.collect.ImmutableList;
import net.trilogy.arch.adapter.structurizr.StructurizrViewsMapper;
import net.trilogy.arch.facade.FilesFacade;
import net.trilogy.arch.transformation.enhancer.DecisionEnhancer;
import net.trilogy.arch.transformation.enhancer.DocumentationEnhancer;
import net.trilogy.arch.transformation.enhancer.ModelEnhancer;

import java.io.File;

public abstract class TransformerFactory {
    public static ArchitectureDataStructureTransformer create(File documentRoot) {
        return new ArchitectureDataStructureTransformer(
                ImmutableList.of(
                        new DocumentationEnhancer(documentRoot, new FilesFacade()),
                        new DecisionEnhancer(),
                        new ModelEnhancer()
                ),
                new StructurizrViewsMapper(documentRoot));
    }
}
