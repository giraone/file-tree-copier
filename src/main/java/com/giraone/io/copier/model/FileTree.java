package com.giraone.io.copier.model;

import com.giraone.io.copier.SourceFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FileTree<T extends SourceFile> {

    private final FileTreeNode<T> root;

    public FileTree(FileTreeNode<T> root) {
        this.root = root;
    }

    public Stream<FileTreeNode<T>> getChildren() {
        return root.getChildren();
    }

    /**
     * Returned the tree of nodes in top-down order (folder1, folder1/file1, ..., folder2, folder2/file1, ...).
     * @return list of nodes
     */
    public List<FileTreeNode<T>> getRecursiveFileList() {
        final ArrayList<FileTreeNode<T>> ret = new ArrayList<>();
        root.getRecursiveFileList(ret);
        return ret;
    }

    public static class FileTreeNode<T> {
        private final T data;
        private final FileTreeNode<T> parent;
        private List<FileTreeNode<T>> children;

        public FileTreeNode(T data, FileTreeNode<T> parent) {
            this.data = data;
            this.parent = parent;
            this.children = null;
        }

        public T getData() {
            return data;
        }

        public FileTreeNode<T> getParent() {
            return parent;
        }

        public void addChild(FileTreeNode<T> child) {
            if (children == null) {
                children = new ArrayList<>();
            }
            children.add(child);
        }

        public boolean hasChildren() {
            return children != null;
        }

        public Stream<FileTreeNode<T>> getChildren() {
            if (children == null) {
                throw new IllegalStateException("Node has " + data + " has no children!");
            }
            return children.stream();
        }

        /**
         * Provide a recursive file list in top-down order.
         * @param lst the list to which the files are added
         */
        public void getRecursiveFileList(List<FileTreeNode<T>> lst) {
            if (children == null) {
                throw new IllegalStateException("Node has " + data + " has no children!");
            }
            children.forEach(c -> {
                if (c.hasChildren()) {
                    c.getRecursiveFileList(lst);
                } else {
                    lst.add(c);
                }
            });
        }
    }
}
