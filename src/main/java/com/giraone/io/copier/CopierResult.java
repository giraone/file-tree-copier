package com.giraone.io.copier;

public class CopierResult {

    private int directoriesCreated = 0;
    private int filesCopied = 0;
    private long bytesCopied = 0L;
    private long provideTimeMillis;
    private long copyTimeMillis;
    private long startTimeMillis;

    public CopierResult() {
        this.startTimeMillis = System.currentTimeMillis();
    }

    public void directoryCreated() {
        directoriesCreated++;
    }

    public void fileCopied(long bytes) {
        bytesCopied += bytes;
        filesCopied++;
    }

    public void finishedProvide() {
        final long end = System.currentTimeMillis();
        this.provideTimeMillis = end - startTimeMillis;
        this.startTimeMillis = end;
    }
    public void finishedCopy() {
        this.copyTimeMillis = System.currentTimeMillis() - startTimeMillis;
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

    public long getProvideTimeMillis() {
        return provideTimeMillis;
    }

    public long getCopyTimeMillis() {
        return copyTimeMillis;
    }

    public long getTotalTimeMillis() {
        return provideTimeMillis + copyTimeMillis;
    }

    @Override
    public String toString() {
        return "CopierResult{" +
            "directoriesCreated=" + directoriesCreated +
            ", filesCopied=" + filesCopied +
            ", bytesCopied=" + bytesCopied +
            ", totalTimeMillis=" + getTotalTimeMillis() +
            '}';
    }
}
