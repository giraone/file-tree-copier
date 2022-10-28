package com.giraone.io.copier.common;

/**
 * Miscellaneous utility methods for working with (file/url) path structures based on slashes (/).
 */
public class PathUtils {

    /**
     * Returns the name of the file or directory denoted by this path structure.
     * The file name is the <em>farthest</em> element from the root in the directory hierarchy.
     * If the {@code path} ends with a slash (/) it is ignored and stripped of.
     *
     * @param path the input path as a string
     * @return a path representing the name of the file or directory, or {@code path} itself if
     * the input is null or the empty string.
     */
    public static String getFileName(String path) {
        if (path == null) {
            return null;
        }
        final int len = path.length();
        if (len == 0) {
            return path;
        }
        int i = path.lastIndexOf('/');
        if (i == len - 1) {
            return getFileName(path.substring(0, len - 1));
        }
        return i >= 0 ? path.substring(i + 1) : path;
    }

    /**
     * Return the file extension of a file name or path name. The returned extension, if available, has always a dot prefix,
     * so the file extension of "image.png" is ".png", not "png".
     * If the given input has only one dot (.) at position 0, this is not considered as a file extension!
     * If the given input has a dot (.) at the last position, "." is returned.
     *
     * @param path the input path
     * @return an extension, if available, null else
     */
    public static String getFileExtension(String path) {
        if (path == null || path.length() == 0) {
            return null;
        }
        final String fileName = getFileName(path);
        int i = fileName.lastIndexOf('.');
        return i > 0 ? fileName.substring(i) : null;
    }
}
