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

    /**
     * Derive the relative path of the file created within the target directory, when the
     * source node is given. A typical example for HTTP would be
     * <pre>
     *     ROOT URL     = http://localhost:8080/subdir1
     *     fileTreeNode = http://localhost:8080/subdir1/subdir11/file.txt
     *     RESULT       = subdir11/file.txt
     * </pre>
     * @param fileTreeNode the source node
     * @return the relative path
     */
    String calculateRelativeTargetFilePath(T fileTreeNode);

    /**
     * Return the implementation used to read from a URL. For file tree provided by a web server,
     * this is typically an HTTP client implementation. For reading trees from other sources,
     * e.g. file trees or class path trees, the default implementation
     * {@link com.giraone.io.copier.resource.DirectReadFromUrlStreamProvider} can be used.
     * @return the providing implementation
     */
    ReadFromUrlStreamProvider getReadFromUrlInputStreamProvider();
}
