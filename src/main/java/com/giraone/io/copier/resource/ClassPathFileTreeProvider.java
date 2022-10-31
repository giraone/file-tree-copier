package com.giraone.io.copier.resource;

import com.giraone.io.copier.AbstractFileTreeProvider;
import com.giraone.io.copier.ReadFromUrlStreamProvider;
import com.giraone.io.copier.common.ResourceWalker;
import com.giraone.io.copier.model.FileTree;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClassPathFileTreeProvider extends AbstractFileTreeProvider<ClassPathResourceFile> {

    private static final ReadFromUrlStreamProvider readFromUrlStreamProvider = new DirectReadFromUrlStreamProvider();

    // always without trailing /
    private final String rootResourcePath;

    public ClassPathFileTreeProvider(String rootResourcePath) {
        while (rootResourcePath.endsWith("/")) {
            rootResourcePath = rootResourcePath.substring(0, rootResourcePath.length() - 1);
        }
        this.rootResourcePath = rootResourcePath;
    }

    @Override
    public FileTree<ClassPathResourceFile> provideTree() {

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

    @Override
    public ReadFromUrlStreamProvider getReadFromUrlInputStreamProvider() {
        return readFromUrlStreamProvider;
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
            if (isDirectory) {
                if (sourceTraverseFilterFunction == null || sourceTraverseFilterFunction.apply(childClassPathResourceFile)) {
                    fileTreeNode.addChild(childFileTreeNode);
                    provideTreeFromResourceLookup(childPath, childFileTreeNode);
                }
            } else {
                if (sourceFileFilterFunction == null || sourceFileFilterFunction.apply(childClassPathResourceFile)) {
                    fileTreeNode.addChild(childFileTreeNode);
                }
            }
        }
    }
}
