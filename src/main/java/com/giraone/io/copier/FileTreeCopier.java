package com.giraone.io.copier;

import com.giraone.io.copier.copy.FileCopy;
import com.giraone.io.copier.model.FileTree;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class FileTreeCopier<T extends SourceFile> {

    private FileTreeProvider<T> fileTreeProvider;
    private File targetDirectory;
    private boolean flattenTree = false;
    private boolean stopOnErrors = false;

    public FileTreeCopier() {
    }

    public FileTreeCopier withFileTreeProvider(FileTreeProvider<T> fileTreeProvider) {
        this.fileTreeProvider = fileTreeProvider;
        return this;
    }

    public FileTreeCopier withTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
        return this;
    }

    public FileTreeCopier withFlattenTree(boolean flattenTree) {
        this.flattenTree = flattenTree;
        return this;
    }

    public CopierResult copy() {

        FileTree<T> sourceTree = this.fileTreeProvider.provideTree();
        return copy(sourceTree);
    }

    protected CopierResult copy(FileTree<T> sourceTree) {

        final Stream<FileTree.FileTreeNode<T>> stream = sourceTree.traverse();
        final CopierResult copierResult = new CopierResult();
        stream.forEach(node -> {
            copy(node, copierResult);
        });

        return copierResult;
    }

    protected void copy(FileTree.FileTreeNode<T> node, CopierResult copierResult) {

        final T nodeData = node.getData();
        final String targetFilePath = fileTreeProvider.calculateRelativeTargetFilePath(nodeData);
        if (targetFilePath == null) {
            throw new RuntimeException("targetFilePath of " + nodeData + " was null!");
        }
        final File targetFile = new File(targetDirectory, targetFilePath);
        if (node.getData().isDirectory()) {
            boolean ok = targetFile.mkdir();
            if (!ok) {
                throw new RuntimeException("Cannot create temp directory \"" + targetFile + "\"!");
            }
            copierResult.directoryCreated();
            node.traverse().forEach(childNode -> {
                copy(childNode, copierResult);
            });
        } else {
            final URL url = node.getData().getUrl();
            try {
                final long bytesCopied = FileCopy.copyUrlContentToFile(url, targetFile);
                copierResult.fileCopied(bytesCopied);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
