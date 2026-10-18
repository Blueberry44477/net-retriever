package io.github.blueberry44477.net_retriever.patterns.structural.composite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;

/**
 * Specialized Leaf node representing a file with primary and backup mirror download sources.
 */
@Getter
public class MirroredFileDownloadItem implements DownloadItem {

    private final String name;
    private final String primaryUrl;
    private final List<String> mirrorUrls;
    private final long totalBytes;
    private long downloadedBytes;
    private boolean active;

    public MirroredFileDownloadItem(String name, String primaryUrl, List<String> mirrorUrls, long totalBytes, long downloadedBytes) {
        this.name = name;
        this.primaryUrl = primaryUrl;
        this.mirrorUrls = (mirrorUrls != null) ? new ArrayList<>(mirrorUrls) : new ArrayList<>();
        this.totalBytes = Math.max(0, totalBytes);
        this.downloadedBytes = Math.clamp(downloadedBytes, 0, this.totalBytes);
    }

    @Override
    public void start() {
        System.out.println("[MirroredFileDownloadItem] [Stub] start() - Starting mirrored download: name=\""
                + name + "\", primary=\"" + primaryUrl + "\", mirrors=" + mirrorUrls.size());
        this.active = true;
    }

    @Override
    public void pause() {
        System.out.println("[MirroredFileDownloadItem] [Stub] pause() - Pausing mirrored download: name=\"" + name + "\"");
        this.active = false;
    }

    @Override
    public void cancel() {
        System.out.println("[MirroredFileDownloadItem] [Stub] cancel() - Canceling mirrored download: name=\"" + name + "\"");
        this.active = false;
        this.downloadedBytes = 0;
    }

    @Override
    public void printHierarchy(String indent) {
        System.out.printf("%s├── [Mirrored File] %s (%d mirrors, %d/%d bytes, %.1f%%)%n",
                indent, name, mirrorUrls.size(), downloadedBytes, totalBytes, getProgressPercentage());
    }

    public List<String> getMirrorUrls() {
        return Collections.unmodifiableList(mirrorUrls);
    }

    public void simulateProgress(long bytes) {
        this.downloadedBytes = Math.min(totalBytes, this.downloadedBytes + bytes);
    }
}
