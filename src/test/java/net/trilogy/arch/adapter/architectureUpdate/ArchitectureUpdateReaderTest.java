package net.trilogy.arch.adapter.architectureUpdate;

import net.trilogy.arch.TestHelper;
import net.trilogy.arch.domain.architectureUpdate.ArchitectureUpdate;
import net.trilogy.arch.domain.architectureUpdate.TddContent;
import net.trilogy.arch.facade.FilesFacade;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.nio.file.Path;

import static org.hamcrest.Matchers.equalTo;

public class ArchitectureUpdateReaderTest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();
    private Path rootDir;

    @Before
    public void setUp() {
        rootDir = TestHelper.getPath(getClass(), TestHelper.ROOT_PATH_TO_TEST_AU_DIRECTORY_STRUCTURE);
    }

    @Test
    public void shouldReadProductArchitectureAndMarkdownFiles() throws Exception {
        Path auDir = rootDir.resolve("architecture-updates/sample/");

        ArchitectureUpdate architectureUpdate = new ArchitectureUpdateReader(new FilesFacade()).load(auDir);

        collector.checkThat(architectureUpdate.getName(), equalTo("test"));
        collector.checkThat(architectureUpdate.getTddContents().get(0), equalTo(new TddContent("" +
                "## TDD 1.2\n" +
                "### Content\n" +
                "**Lorem ipsum** dolor sit amet:\n" +
                "* consectetur adipiscing elit\n" +
                "* sed do eiusmod tempor *incididunt ut labore* et dolore magna aliqua\n" +
                "* et ligula ullamcorper malesuada proin libero nunc consequat\n",
                "TDD 1.2 : Component-16.md")));
    }
}
