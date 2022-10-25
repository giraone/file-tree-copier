package com.giraone.io.copier.resource;

import com.giraone.io.copier.AbstractFileTreeProvider;
import com.giraone.io.copier.model.FileTree;

public class ClassPathFileTreeProvider extends AbstractFileTreeProvider<ClassPathResourceFile> {

    private String resourcePath;
    private String fileExtensionFilter;

    public ClassPathFileTreeProvider(String resourcePath) {
        this.resourcePath = resourcePath;
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
        return null;
    }
}
