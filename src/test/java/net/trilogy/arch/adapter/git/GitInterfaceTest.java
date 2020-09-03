package net.trilogy.arch.adapter.git;

import net.trilogy.arch.TestHelper;
import net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GitInterfaceTest {
    @Rule
    public final ErrorCollector collector = new ErrorCollector();
    private final String masterCommitTagName = "masterCommitTagName";
    private File repoDir;
    private File rootDir;
    private Path archPath;
    private String architectureAsString;
    private String masterCommitSha;

    @After
    public void tearDown() throws Exception {
        FileUtils.forceDelete(repoDir);
    }

    @Before
    public void setUp() throws Exception {
        repoDir = Files.createTempDirectory("aac").toFile();
        var git = Git.init().setDirectory(repoDir).call();

        rootDir = Files.createDirectory(repoDir.toPath().resolve("root")).toFile();

        archPath = rootDir.toPath().resolve("./product-architecture.yml"); // the "./" is added to ensure relative pathing works

        architectureAsString = TestHelper.loadResource(getClass(), TestHelper.MANIFEST_PATH_TO_TEST_GENERALLY);
        // First commit
        Files.writeString(archPath, "initial commit");
        git.add().addFilepattern("root/product-architecture.yml").call();
        git.commit().setMessage("initial commit").call();

        // Second commit
        Files.writeString(archPath, architectureAsString);
        git.add().addFilepattern("root/product-architecture.yml").call();
        final RevCommit commit = git.commit().setMessage("commit architecture to master").call();
        masterCommitSha = commit.getName();
        git.tag().setAnnotated(true).setName(masterCommitTagName).setMessage("blah").call();

        collector.checkThat(
                "PRECONDITION CHECK: Architecture must exist in master branch.",
                Files.exists(archPath),
                is(true)
        );

        git.checkout().setCreateBranch(true).setName("not-master").call();
        Files.delete(archPath);
        git.add().setUpdate(true).addFilepattern("root/product-architecture.yml").call();
        git.commit().setMessage("commit deleting architecture in other branch").call();

        collector.checkThat(
                "PRECONDITION CHECK: Architecture must not exist in current branch.",
                Files.exists(archPath),
                is(false)
        );
    }

    @Test
    public void shouldLoadArchitectureFromBranch() throws Exception {
        var actual = new GitInterface().load("master", archPath);
        var expected = new ArchitectureDataStructureObjectMapper().readValue(architectureAsString);

        collector.checkThat(actual, equalTo(expected));
    }

    @Test
    public void shouldLoadArchitectureFromCommit() throws Exception {
        var actual = new GitInterface().load(masterCommitSha, archPath);
        var expected = new ArchitectureDataStructureObjectMapper().readValue(architectureAsString);

        collector.checkThat(actual, equalTo(expected));
    }

    @Test
    public void shouldLoadArchitectureFromTag() throws Exception {
        var actual = new GitInterface().load(masterCommitTagName, archPath);
        var expected = new ArchitectureDataStructureObjectMapper().readValue(architectureAsString);

        collector.checkThat(actual, equalTo(expected));
    }

    @Test
    public void shouldNotChangeBranch() throws Exception {
        new GitInterface().load("master", archPath);

        collector.checkThat(isBranch("not-master"), is(true));
    }

    @Test
    public void shouldPreserveWorkingDirFiles() throws Exception {
        var otherFile = archPath.getParent().resolve("other-file.txt");
        Files.writeString(archPath, "MODIFIED FILE");
        Files.writeString(otherFile, "NEW FILE");
        var beforeFiles = FileUtils.listFilesAndDirs(
                repoDir,
                FileFilterUtils.trueFileFilter(),
                FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(".git")));

        new GitInterface().load("master", archPath);

        var afterFiles = FileUtils.listFilesAndDirs(
                repoDir,
                FileFilterUtils.trueFileFilter(),
                FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(".git")));

        collector.checkThat(beforeFiles, equalTo(afterFiles));
        collector.checkThat(Files.readString(archPath), equalTo("MODIFIED FILE"));
        collector.checkThat(Files.readString(otherFile), equalTo("NEW FILE"));
    }

    @Test
    public void shouldNotChangeBranchIfException() throws Exception {
        var badMapper = mock(ArchitectureDataStructureObjectMapper.class);
        when(badMapper.readValue(any())).thenThrow(new RuntimeException("Boom!"));

        try {
            new GitInterface(badMapper).load("master", archPath);
            fail("Exception not thrown.");
        } catch (RuntimeException ignored) {
            collector.checkThat(isBranch("not-master"), is(true));
        }
    }

    @Test
    public void shouldPreserveWorkingDirFilesIfException() throws Exception {
        var badMapper = mock(ArchitectureDataStructureObjectMapper.class);
        when(badMapper.readValue(any())).thenThrow(new RuntimeException("Boom!"));

        var otherFile = archPath.getParent().resolve("other-file.txt");
        Files.writeString(archPath, "MODIFIED FILE");
        Files.writeString(otherFile, "NEW FILE");
        var beforeFiles = FileUtils.listFilesAndDirs(
                repoDir,
                FileFilterUtils.trueFileFilter(),
                FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(".git")));

        try {
            new GitInterface(badMapper).load("master", archPath);
            fail("Exception not thrown.");
        } catch (RuntimeException ignored) {
            var afterFiles = FileUtils.listFilesAndDirs(
                    repoDir,
                    FileFilterUtils.trueFileFilter(),
                    FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter(".git")));

            collector.checkThat(beforeFiles, equalTo(afterFiles));
            collector.checkThat(Files.readString(archPath), equalTo("MODIFIED FILE"));
            collector.checkThat(Files.readString(otherFile), equalTo("NEW FILE"));
        }
    }

    @Test(expected = GitInterface.BranchNotFoundException.class)
    public void shouldHandleIfBranchInvalid() throws Exception {
        new GitInterface().load("non-existent-branch", archPath);
    }

    @Test
    public void shouldReturnCorrectBranch() throws Exception {
        collector.checkThat(new GitInterface().getBranch(rootDir), equalTo("not-master"));
    }

    @Test(expected = GitInterface.BranchNotFoundException.class)
    public void shouldThrowWhenNotGitRepo() throws Exception {
        File noGitDir = Files.createTempDirectory("aac").toFile();

        new GitInterface().getBranch(noGitDir);
    }

    private boolean isBranch(String wantedBranch) throws Exception {
        return new GitInterface()
                .getBranch(archPath.toFile().getParentFile())
                .equals(wantedBranch);
    }
}
