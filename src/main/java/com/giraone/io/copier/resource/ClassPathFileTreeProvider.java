package com.giraone.io.copier.resource;

import com.giraone.io.copier.AbstractFileTreeProvider;
import com.giraone.io.copier.FileTreeProvider;
import com.giraone.io.copier.SourceFile;
import com.giraone.io.copier.common.ResourceWalker;
import com.giraone.io.copier.model.FileTree;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class ClassPathFileTreeProvider extends AbstractFileTreeProvider<ClassPathResourceFile> {

    private final String rootResourcePath;
    private Function<SourceFile, Boolean> sourceFileFilterFunction;

    public ClassPathFileTreeProvider(String rootResourcePath) {
        this.rootResourcePath = rootResourcePath;
    }

    @Override
    public FileTreeProvider<ClassPathResourceFile> withFilter(Function<SourceFile, Boolean> sourceFileFilterFunction) {
        this.sourceFileFilterFunction = sourceFileFilterFunction;
        return this;
    }

    @Override
    public FileTree provideTree() {

        ClassPathResourceFile rootFile = new ClassPathResourceFile(this.rootResourcePath);
        final FileTree.FileTreeNode<ClassPathResourceFile> fileTreeNode = new FileTree.FileTreeNode<>(rootFile, null);
        provideTreeFromResourceLookup(this.rootResourcePath, fileTreeNode);
        return new FileTree<>(fileTreeNode);
    }

    @Override
    public String calculateRelativeTargetFilePath(ClassPathResourceFile fileTreeNode) {

        final String path = fileTreeNode.getResourcePath();
        return extractNeededChildPath(rootResourcePath, path);
    }

    //------------------------------------------------------------------------------------------------------------------

    protected void provideTreeFromResourceLookup(String resourcePath, FileTree.FileTreeNode<ClassPathResourceFile> fileTreeNode) {

        final List<File> children = new ArrayList<>(16);
        AtomicBoolean firstSkipped = new AtomicBoolean();
        final ResourceWalker resourceWalker = new ResourceWalker();
        resourceWalker.walk(resourcePath, 1, path -> {
            // skip the first entry - this is the node itself, no one of the children
            if (firstSkipped.get()) {
                children.add(path);
            } else {
                firstSkipped.set(true);
            }
        });

        for (File childFile : children) {
            final boolean isDirectory = childFile.isDirectory();
            final String fileName = childFile.getName();
            final String childPath = resourcePath + "/" + fileName;
            final ClassPathResourceFile childClassPathResourceFile = new ClassPathResourceFile(childPath, fileName, isDirectory);
            final FileTree.FileTreeNode<ClassPathResourceFile> childFileTreeNode = new FileTree.FileTreeNode<>(
                childClassPathResourceFile, fileTreeNode);
            if (sourceFileFilterFunction == null || sourceFileFilterFunction.apply(childClassPathResourceFile)) {
                fileTreeNode.addChild(childFileTreeNode);
                if (isDirectory) {
                    provideTreeFromResourceLookup(childPath, childFileTreeNode);
                }
            }
        }
    }
}
