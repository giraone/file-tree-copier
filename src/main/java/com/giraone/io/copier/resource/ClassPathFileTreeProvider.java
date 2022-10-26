package com.giraone.io.copier.resource;

import com.giraone.io.copier.AbstractFileTreeProvider;
import com.giraone.io.copier.FileTreeProvider;
import com.giraone.io.copier.SourceFile;
import com.giraone.io.copier.model.FileTree;

import java.util.function.Function;

public class ClassPathFileTreeProvider extends AbstractFileTreeProvider<ClassPathResourceFile> {

    private String resourcePath;
    private Function<SourceFile, Boolean> sourceFileFilterFunction;

    public ClassPathFileTreeProvider(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    @Override
    public FileTreeProvider<ClassPathResourceFile> withFilter(Function<SourceFile, Boolean> sourceFileFilterFunction) {
        this.sourceFileFilterFunction = sourceFileFilterFunction;
        return this;
    }

    @Override
    public FileTree provideTree() {

        ClassPathResourceFile rootFile = new ClassPathResourceFile(this.resourcePath);
        final FileTree.FileTreeNode<ClassPathResourceFile> fileTreeNode = new FileTree.FileTreeNode<>(rootFile, null);
        FileTree<ClassPathResourceFile> ret = new FileTree<>(fileTreeNode);


        return ret;
    }

    @Override
    public String calculateRelativeTargetFilePath(ClassPathResourceFile fileTreeNode) {

        final String path = fileTreeNode.getResourcePath();
        return extractNeededChildPath(resourcePath, path);
    }
}
