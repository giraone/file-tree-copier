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

    public Stream<FileTreeNode<T>> traverse() {
        return root.traverse();
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

        public Stream<FileTreeNode<T>> traverse() {
            if (children == null) {
                throw new IllegalStateException("Node has " + data + " has no children!");
            }
            return children.stream();
        }
    }
}
