package com.giraone.io.copier.web;

import com.giraone.io.copier.AbstractFileTreeProvider;
import com.giraone.io.copier.FileTreeProvider;
import com.giraone.io.copier.SourceFile;
import com.giraone.io.copier.model.FileTree;
import com.giraone.io.copier.web.index.AutoIndexItem;
import com.giraone.io.copier.web.index.AutoIndexItemType;
import com.giraone.io.copier.web.index.AutoIndexReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.function.Function;

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
    private Function<SourceFile, Boolean> sourceFileFilterFunction = null;
    private final List<AutoIndexItem> children = null;

    public WebServerFileTreeProvider(URL rootUrl) {
        this.rootUrl = rootUrl;
    }

    @Override
    public FileTreeProvider<WebServerFile> withFilter(Function<SourceFile, Boolean> sourceFileFilterFunction) {
        this.sourceFileFilterFunction = sourceFileFilterFunction;
        return this;
    }

    @Override
    public FileTree<WebServerFile> provideTree() {

        final WebServerFile rootFile = new WebServerFile(rootUrl);
        final FileTree.FileTreeNode<WebServerFile> fileTreeNode = new FileTree.FileTreeNode<>(rootFile, null);
        provideTreeFromAutoIndex(rootUrl, fileTreeNode);
        final FileTree<WebServerFile> ret = new FileTree<>(fileTreeNode);
        return ret;
    }

    @Override
    public String calculateRelativeTargetFilePath(WebServerFile fileTreeNode) {

        final String path = fileTreeNode.getUrl().getPath();
        return extractNeededChildPath(rootUrl.getPath(), path);
    }

    //------------------------------------------------------------------------------------------------------------------

    protected void provideTreeFromAutoIndex(URL url, FileTree.FileTreeNode<WebServerFile> fileTreeNode) {

        final List<AutoIndexItem> children = read(url);
        children.forEach(child -> {
            final URL childUrl = childUrl(url, child.getName(), child.isDirectory());
            final WebServerFile childFile = new WebServerFile(childUrl, child.getName(), child.isDirectory());
            final FileTree.FileTreeNode<WebServerFile> childFileTreeNode = new FileTree.FileTreeNode<>(childFile, fileTreeNode);
            if (sourceFileFilterFunction == null || sourceFileFilterFunction.apply(childFile)) {
                fileTreeNode.addChild(childFileTreeNode);
                if (child.getType() == AutoIndexItemType.directory) {
                    provideTreeFromAutoIndex(childUrl, childFileTreeNode);
                }
            }
        });
    }

    private List<AutoIndexItem> read(URL url) {

        InputStream in;
        try {
            in = url.openStream();
        } catch (IOException e) {
            throw new RuntimeException("Cannot open URL \"" + url + "\"", e);
        }
        try {
            return autoIndexReader.read(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
