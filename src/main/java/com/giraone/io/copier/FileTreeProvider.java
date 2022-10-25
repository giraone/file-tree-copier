package com.giraone.io.copier;

import com.giraone.io.copier.model.FileTree;

public interface FileTreeProvider<T extends SourceFile> {

    FileTree<T> provideTree();

    String calculateRelativeTargetFilePath(T fileTreeNode);
}
