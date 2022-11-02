package com.giraone.io.copier;

import com.giraone.io.copier.copy.FileCopy;
import com.giraone.io.copier.model.FileTree;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.stream.Stream;

/**
 * Utility class for copying a file tree from web server or from classpath resource to a (local) file system.
 * Hints:
 * <ul>
 *     <li>Copying will not create subdirectories, that are totally empty (no directories, no files)</li>
 * </ul>
 *
 * @param <T> the file tree node implementation
 */
public class FileTreeCopier<T extends SourceFile> {

    private FileTreeProvider<T> fileTreeProvider;
    private File targetDirectory;
    private boolean flatCopy = false;

    public FileTreeCopier() {
    }

    /**
     * Define the file tree provider to read the tree and content
     *
     * @param fileTreeProvider FileTreeProvider implementation
     * @return this
     */
    @SuppressWarnings("UnusedReturnValue")
    public FileTreeCopier withFileTreeProvider(FileTreeProvider<T> fileTreeProvider) {
        this.fileTreeProvider = fileTreeProvider;
        return this;
    }

    /**
     * Define the target directory to copy to.
     *
     * @param targetDirectory the target directory, that must exists
     * @return this
     */
    @SuppressWarnings("UnusedReturnValue")
    public FileTreeCopier withTargetDirectory(File targetDirectory) {
        if (targetDirectory == null) {
            throw new IllegalArgumentException("Target directory cannot be null!");
        }
        if (!targetDirectory.exists()) {
            throw new IllegalArgumentException("Target directory \"" + targetDirectory + "\" must exist!");
        }
        if (!targetDirectory.isDirectory()) {
            throw new IllegalArgumentException("Target directory \"" + targetDirectory + "\" must be a directory!");
        }
        this.targetDirectory = targetDirectory;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public FileTreeCopier withFlatCopy() {
        this.flatCopy = true;
        return this;
    }

    public CopierResult copy() {
        final CopierResult copierResult = new CopierResult();
        final FileTree<T> sourceTree = this.fileTreeProvider.provideTree();
        copierResult.finishedProvide();
        return copy(sourceTree, copierResult);
    }

    protected CopierResult copy(FileTree<T> sourceTree, CopierResult copierResult) {
        final Stream<FileTree.FileTreeNode<T>> stream = sourceTree.getChildrenNodesAsStream();
        if (this.flatCopy) {
            stream.forEach(node -> copyFlat(node, copierResult));
        } else {
            stream.forEach(node -> copy(node, copierResult));
        }
        copierResult.finishedCopy();
        return copierResult;
    }

    protected void copyFlat(FileTree.FileTreeNode<T> node, CopierResult copierResult) {
        final T nodeData = node.getData();
        if (nodeData.isDirectory()) {
            if (node.hasChildren()){
                node.getChildrenNodesAsStream().forEach(childNode -> copyFlat(childNode, copierResult));
            }
            return;
        }
        final File targetFile = new File(targetDirectory, nodeData.getName());
        final URL url = node.getData().getUrl();
        try {
            final long bytesCopied = FileCopy.copyUrlContentToFile(url,
                fileTreeProvider.getReadFromUrlInputStreamProvider(), targetFile);
            copierResult.fileCopied(bytesCopied);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void copy(FileTree.FileTreeNode<T> node, CopierResult copierResult) {
        final T nodeData = node.getData();
        final String targetFilePath = fileTreeProvider.calculateRelativeTargetFilePath(nodeData);
        if (targetFilePath == null) {
            throw new RuntimeException("targetFilePath of " + nodeData + " was null!");
        }
        final File targetFile = new File(targetDirectory, targetFilePath);
        if (nodeData.isDirectory()) {
            if (node.hasChildren()) {
                // Create a directory only, when there are children
                boolean ok = targetFile.mkdir();
                if (!ok) {
                    throw new RuntimeException("Cannot create temp directory \"" + targetFile + "\"!");
                }
                copierResult.directoryCreated();
                node.getChildrenNodesAsStream().forEach(childNode -> copy(childNode, copierResult));
            }
        } else {
            final URL url = node.getData().getUrl();
            try {
                final long bytesCopied = FileCopy.copyUrlContentToFile(url,
                    fileTreeProvider.getReadFromUrlInputStreamProvider(), targetFile);
                copierResult.fileCopied(bytesCopied);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
