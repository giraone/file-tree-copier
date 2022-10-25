package com.giraone.io.copier;

public abstract class AbstractFileTreeProvider<T extends SourceFile> implements FileTreeProvider<T> {

    protected static String extractNeededParentPath(String parentSourcePath, String resourceRoot) {

        // be tolerant with trailing slashes
        while (parentSourcePath.endsWith("/")) {
            parentSourcePath = parentSourcePath.substring(0, parentSourcePath.length() - 1);
        }
        while (resourceRoot.endsWith("/")) {
            resourceRoot = resourceRoot.substring(0, resourceRoot.length() - 1);
        }

        final StringBuilder sb = new StringBuilder();
        while (true) {
            if (parentSourcePath.endsWith(resourceRoot)) {
                return sb.toString();
            }
            int i = parentSourcePath.lastIndexOf('/');
            if (i < 1) {
                return null;
            }
            sb.insert(0, "/").insert(0, parentSourcePath.substring(i + 1));
            parentSourcePath = parentSourcePath.substring(0, i);
        }
    }

    protected static String extractNeededChildPath(String parentSourcePath, String resourceRoot) {

        // be tolerant with trailing slashes in parent path
        while (parentSourcePath.endsWith("/")) {
            parentSourcePath = parentSourcePath.substring(0, parentSourcePath.length() - 1);
        }
        // be tolerant with trailing slashes in resource path
        while (resourceRoot.endsWith("/")) {
            resourceRoot = resourceRoot.substring(0, resourceRoot.length() - 1);
        }
        // be tolerant with leading slashes in resource path
        while (resourceRoot.startsWith("/")) {
            resourceRoot = resourceRoot.substring(1);
        }

        final StringBuilder sb = new StringBuilder();
        while (true) {
            if (parentSourcePath.endsWith(resourceRoot)) {
                return sb.toString();
            }
            int i = resourceRoot.lastIndexOf('/');
            if (i < 0) {
                if (parentSourcePath.length() == 0) {
                    return resourceRoot;
                } else {
                    return null;
                }
            }
            if (i == 0) {
                return resourceRoot.substring(1);
            }
            if (sb.length() > 0) {
                sb.insert(0, '/');
            }
            sb.insert(0, resourceRoot.substring(i + 1));
            resourceRoot = resourceRoot.substring(0, i);
        }
    }
}
