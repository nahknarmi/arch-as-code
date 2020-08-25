package net.trilogy.arch.adapter.git;

import net.trilogy.arch.adapter.architectureDataStructure.ArchitectureDataStructureObjectMapper;
import net.trilogy.arch.domain.ArchitectureDataStructure;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class GitInterface {
    private final ArchitectureDataStructureObjectMapper objMapper;

    public GitInterface() {
        this(new ArchitectureDataStructureObjectMapper());
    }

    GitInterface(ArchitectureDataStructureObjectMapper objMapper) {
        this.objMapper = objMapper;
    }

    public ArchitectureDataStructure load(String commitReference, Path architectureYamlFilePath)
            throws IOException, GitAPIException, BranchNotFoundException {

        var git = openParentRepo(architectureYamlFilePath.toFile());
        final RevCommit commit = getCommitFrom(git, commitReference);
        final String relativePath = getRelativePath(architectureYamlFilePath, git);
        final String archAsString = getContent(git, commit, relativePath);

        return objMapper.readValue(archAsString);
    }

    private RevCommit getCommitFrom(Git git, String commitReference) throws GitAPIException, IOException, BranchNotFoundException {

        var objId = git.getRepository().resolve(commitReference);
        if (objId == null) {
            throw new BranchNotFoundException();
        }

        if(!isAnnotatedTag(git, objId)){
            return git.log().add(objId).call().iterator().next();
        }

        var realObjId = git.getRepository()
                        .getRefDatabase()
                        .peel(git.getRepository().getRefDatabase().findRef(commitReference))
                        .getPeeledObjectId();

        return git.log().add(realObjId).call().iterator().next();
    }

    public String getBranch(File dir) throws BranchNotFoundException {
        try {
            return openParentRepo(dir)
                    .getRepository()
                    .getBranch();
        } catch (Exception e) {
            throw new BranchNotFoundException();
        }
    }

    private String getRelativePath(Path architectureYamlFilePath, Git git) {
        var repoDirAbsolutePath = git.getRepository()
            .getDirectory()
            .getParentFile()
            .toPath()
            .toAbsolutePath() 
            .normalize()
            .toString();
        return architectureYamlFilePath
                .toAbsolutePath()
                .normalize()
                .toString()
                .replaceAll(repoDirAbsolutePath, "")
                .replaceAll("^/", "");
    }

    private String getContent(Git git, RevCommit commit, String path) throws IOException {
        try (TreeWalk treeWalk = TreeWalk.forPath(git.getRepository(), path, commit.getTree())) {
            ObjectId blobId = treeWalk.getObjectId(0);
            try (ObjectReader objectReader = git.getRepository().newObjectReader()) {
                ObjectLoader objectLoader = objectReader.open(blobId);
                byte[] bytes = objectLoader.getBytes();
                return new String(bytes, StandardCharsets.UTF_8);
            }
        }
    }

    private boolean isAnnotatedTag(Git git, ObjectId resolvedCommitReference) throws IOException {
        return git.getRepository().newObjectReader().open(resolvedCommitReference).getType() == Constants.OBJ_TAG;
    }

    private Git openParentRepo(File dir) throws IOException {
        return Git.wrap(
                new FileRepositoryBuilder()
                        .findGitDir(toAbsolute(dir))
                        .build()
        );
    }

    private static File toAbsolute(File dir) {
        return dir.toPath().toAbsolutePath().toFile();
    }

    public static class BranchNotFoundException extends Exception {
    }
}
