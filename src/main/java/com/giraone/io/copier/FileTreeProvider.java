package com.giraone.io.copier;

import com.giraone.io.copier.model.FileTree;

import java.util.function.Function;

public interface FileTreeProvider<T extends SourceFile> {

    /**
     * Configure the file tree provider to return only files matching filter (e.g. name matches a certain file extension)
     * and to traverse only directories matching the filter.
     *
     * @param sourceFileFilterFunction a filter function on the source file
     * @return the called object
     */
    FileTreeProvider<T> withFilter(Function<SourceFile, Boolean> sourceFileFilterFunction);

    /**
     * Traverse the defined source and return a node tree
     *
     * @return a file tree with directories (branches) and regular files (leafs)
     */
    FileTree<T> provideTree();

    String calculateRelativeTargetFilePath(T fileTreeNode);
}
