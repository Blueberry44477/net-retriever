package io.github.blueberry44477.net_retriever.patterns.structural.composite;

import lombok.Getter;

/**
 * Leaf node in the GoF Composite pattern representing a single downloadable file.
 */
@Getter
public class FileDownloadItem implements DownloadItem {

    private final String name;
    private final String url;
    private final long totalBytes;
    private long downloadedBytes;
    private boolean active;

    public FileDownloadItem(String name, String url, long totalBytes) {
        this(name, url, totalBytes, 0L);
    }

    public FileDownloadItem(String name, String url, long totalBytes, long downloadedBytes) {
        this.name = name;
        this.url = url;
        this.totalBytes = Math.max(0, totalBytes);
        this.downloadedBytes = Math.clamp(downloadedBytes, 0, this.totalBytes);
    }

    @Override
    public void start() {
        System.out.println("[FileDownloadItem] [Stub] start() - Starting file download: name=\""
                + name + "\", url=\"" + url + "\" (" + downloadedBytes + "/" + totalBytes + " bytes)");
        this.active = true;
    }

    @Override
    public void pause() {
        System.out.println("[FileDownloadItem] [Stub] pause() - Pausing file download: name=\"" + name + "\"");
        this.active = false;
    }

    @Override
    public void cancel() {
        System.out.println("[FileDownloadItem] [Stub] cancel() - Canceling file download: name=\"" + name + "\"");
        this.active = false;
        this.downloadedBytes = 0;
    }

    @Override
    public void printHierarchy(String indent) {
        System.out.printf("%s├── [File] %s (%d/%d bytes, %.1f%%)%n",
                indent, name, downloadedBytes, totalBytes, getProgressPercentage());
    }

    public void simulateProgress(long bytes) {
        this.downloadedBytes = Math.min(totalBytes, this.downloadedBytes + bytes);
    }
}
