package net.nahknarmi.arch;

import com.google.common.collect.ImmutableList;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Model;
import net.nahknarmi.arch.domain.c4.C4Person;
import net.nahknarmi.arch.domain.c4.C4SoftwareSystem;
import net.nahknarmi.arch.domain.c4.C4View;

import java.util.Collections;

public abstract class TestHelper {
    public static Long TEST_WORKSPACE_ID = 49344L;
    public static String TEST_SPACES_MANIFEST_PATH = "/architecture/products/testspaces/data-structure.yml";
    public static String TEST_PRODUCT_DOCUMENTATION_ROOT_PATH = "/architecture/products/testspaces/";

    public static String TEST_VALIDATION_ROOT_PATH = "/validation/";


    public static ArchitectureDataStructure getDataStructure(C4Model model) {
        return new ArchitectureDataStructure("name", "business unit", "desc", Collections.emptyList(), model);
    }

    public static C4Model noPersonModel() {
        return new C4Model(Collections.emptyList(), ImmutableList.of(new C4SoftwareSystem()), new C4View());
    }

    public static C4Model noSystemModel() {
        return new C4Model(ImmutableList.of(new C4Person()), Collections.emptyList(), new C4View());
    }

    public static C4Model noSystemNoPersonModel() {
        return new C4Model(Collections.emptyList(), Collections.emptyList(), new C4View());
    }
}
