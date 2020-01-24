package net.nahknarmi.arch.transformation;

import com.google.common.collect.ImmutableList;
import net.nahknarmi.arch.adapter.WorkspaceIdFinder;
import net.nahknarmi.arch.transformation.enhancer.*;

import java.io.File;

public abstract class TransformerFactory {

    public static ArchitectureDataStructureTransformer create(File documentRoot) {
        return new ArchitectureDataStructureTransformer(
                ImmutableList.of(
                        new DocumentationEnhancer(documentRoot),
                        new DecisionEnhancer(),
                        new ModelEnhancer(),
                        new StyleViewEnhancer(),
                        new SystemContextViewEnhancer(),
                        new ContainerContextViewEnhancer(),
                        new ComponentContextViewEnhancer(),
                        new SystemLandscapeViewEnhancer()
                ),
                new WorkspaceIdFinder());
    }
}
