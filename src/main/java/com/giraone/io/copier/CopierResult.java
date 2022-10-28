package com.giraone.io.copier;

public class CopierResult {

    private boolean success = false;
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

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getDirectoriesCreated() {
        return directoriesCreated;
    }

    public void setDirectoriesCreated(int directoriesCreated) {
        this.directoriesCreated = directoriesCreated;
    }

    public int getFilesCopied() {
        return filesCopied;
    }

    public void setFilesCopied(int filesCopied) {
        this.filesCopied = filesCopied;
    }

    public long getBytesCopied() {
        return bytesCopied;
    }

    public void setBytesCopied(long bytesCopied) {
        this.bytesCopied = bytesCopied;
    }

    @Override
    public String toString() {
        return "CopierResult{" +
            "success=" + success +
            ", directoriesCreated=" + directoriesCreated +
            ", filesCopied=" + filesCopied +
            ", bytesCopied=" + bytesCopied +
            '}';
    }
}
