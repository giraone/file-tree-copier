package com.giraone.io.copier;

public class CopierResult {

    private int directoriesCreated = 0;
    private int filesCopied = 0;
    private long bytesCopied = 0L;

    public CopierResult() {
    }

    public void directoryCreated() {
        directoriesCreated++;
    }

    public void fileCopied(long bytes) {
        bytesCopied += bytes;
        filesCopied++;
    }

    public int getDirectoriesCreated() {
        return directoriesCreated;
    }

    public int getFilesCopied() {
        return filesCopied;
    }

    public long getBytesCopied() {
        return bytesCopied;
    }

    @Override
    public String toString() {
        return "CopierResult{" +
            "directoriesCreated=" + directoriesCreated +
            ", filesCopied=" + filesCopied +
            ", bytesCopied=" + bytesCopied +
            '}';
    }
}
