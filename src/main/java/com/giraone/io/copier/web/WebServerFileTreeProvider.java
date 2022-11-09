package com.giraone.io.copier.web;

import com.giraone.io.copier.AbstractFileTreeProvider;
import com.giraone.io.copier.FileTreeProvider;
import com.giraone.io.copier.ReadFromUrlStreamProvider;
import com.giraone.io.copier.model.FileTree;
import com.giraone.io.copier.web.http.DefaultHttpClientInputStreamProvider;
import com.giraone.io.copier.web.index.AutoIndexItem;
import com.giraone.io.copier.web.index.AutoIndexItemType;
import com.giraone.io.copier.web.index.AutoIndexReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * A {@link FileTreeProvider} provided by a web server using JSON index files.
 * E.g. <a href="https://www.nginx.com">Nginx</a> wit
 * <pre>location / {
 *   autoindex on;
 *   autoindex_format json;
 * }
 * </pre>.
 */
public class WebServerFileTreeProvider extends AbstractFileTreeProvider<WebServerFile> {

    private static final AutoIndexReader autoIndexReader = new AutoIndexReader();

    private final URL rootUrl;
    private ReadFromUrlStreamProvider httpClient = new DefaultHttpClientInputStreamProvider();

    public WebServerFileTreeProvider(URL rootUrl) {
        this.rootUrl = rootUrl;
    }

    @SuppressWarnings("UnusedReturnValue")
    public WebServerFileTreeProvider withHttpClient(ReadFromUrlStreamProvider httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    @Override
    public FileTree<WebServerFile> provideTree() {

        final WebServerFile rootFile = new WebServerFile(rootUrl);
        final FileTree.FileTreeNode<WebServerFile> fileTreeNode = new FileTree.FileTreeNode<>(rootFile, null);
        provideTreeFromAutoIndex(rootUrl, fileTreeNode);
        return new FileTree<>(fileTreeNode);
    }

    @Override
    public String calculateRelativeTargetFilePath(WebServerFile fileTreeNode) {

        final String path = fileTreeNode.getUrl().getPath();
        return extractNeededChildPath(rootUrl.getPath(), path);
    }

    @Override
    public ReadFromUrlStreamProvider getReadFromUrlInputStreamProvider() {
        return httpClient;
    }

    //------------------------------------------------------------------------------------------------------------------

    protected void provideTreeFromAutoIndex(URL url, FileTree.FileTreeNode<WebServerFile> fileTreeNode) {

        final List<AutoIndexItem> children = readIndexedContentList(url);
        children.forEach(child -> {
            final URL childUrl = childUrl(url, child.getName(), child.isDirectory());
            final WebServerFile childFile = new WebServerFile(childUrl, child.getName(), child.isDirectory());
            final FileTree.FileTreeNode<WebServerFile> childFileTreeNode = new FileTree.FileTreeNode<>(childFile, fileTreeNode);
            if (child.getType() == AutoIndexItemType.directory) {
                // Traverse child tree, if directory is not filtered
                if (sourceTraverseFilterFunction == null || sourceTraverseFilterFunction.apply(childFile)) {
                    // Add directory node to tree
                    fileTreeNode.addChild(childFileTreeNode);
                    // Provide tree for children
                    provideTreeFromAutoIndex(childUrl, childFileTreeNode);
                }
            } else {
                // Add file node to tree, optionally filtered
                if (sourceFileFilterFunction == null || sourceFileFilterFunction.apply(childFile)) {
                    fileTreeNode.addChild(childFileTreeNode);
                }
            }
        });
    }

    private List<AutoIndexItem> readIndexedContentList(URL url) {

        InputStream in = httpClient.openInputStream(url);
        try {
            return autoIndexReader.read(in);
        } catch (IOException e) {
            throw new RuntimeException("Error reading index for URL \"" + url + "\"!");
        }
    }

    private URL childUrl(URL parent, String childName, boolean isDirectory) {

        String parentUrlString = parent.toExternalForm();
        if (!parentUrlString.endsWith("/")) {
            parentUrlString += "/";
        }
        try {
            return new URL(parentUrlString + childName + (isDirectory ? "/" : ""));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
