package net.nahknarmi.arch;

import com.google.common.collect.ImmutableList;
import net.nahknarmi.arch.domain.ArchitectureDataStructure;
import net.nahknarmi.arch.domain.c4.C4Model;
import net.nahknarmi.arch.domain.c4.C4Person;
import net.nahknarmi.arch.domain.c4.C4SoftwareSystem;

import static java.util.Collections.emptyList;

public abstract class TestHelper {
    public static Long TEST_WORKSPACE_ID = 49344L;
    public static String TEST_SPACES_MANIFEST_PATH = "/architecture/products/testspaces/data-structure.yml";
    public static String TEST_PRODUCT_DOCUMENTATION_ROOT_PATH = "/architecture/products/testspaces/";

    public static String TEST_VALIDATION_ROOT_PATH = "/validation/";
    public static String TEST_VIEW_ROOT_PATH = "/view/bigBank/";


    public static ArchitectureDataStructure getDataStructure(C4Model model) {
//        return new ArchitectureDataStructure("name", "business unit", "desc", emptyList(), model);
        return null;
    }

    public static C4Model noPersonModel() {
        return new C4Model(emptyList(), ImmutableList.of(new C4SoftwareSystem()), emptyList(), emptyList());
    }

    public static C4Model noSystemModel() {
        return new C4Model(ImmutableList.of(new C4Person()), emptyList(), emptyList(), emptyList());
    }

    public static C4Model noSystemNoPersonModel() {
        return new C4Model(emptyList(), emptyList(), emptyList(), emptyList());
    }
}
