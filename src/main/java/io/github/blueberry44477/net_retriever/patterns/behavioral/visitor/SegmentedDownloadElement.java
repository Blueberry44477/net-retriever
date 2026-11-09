package io.github.blueberry44477.net_retriever.patterns.behavioral.visitor;

import java.util.Objects;
import java.util.UUID;

import lombok.Getter;

/**
 * Concrete Element representing a multi-part segmented chunk download.
 */
@Getter
public class SegmentedDownloadElement implements DownloadElement {

    private final String id;
    private final String name;
    private final String url;
    private final long totalBytes;
    private final long downloadedBytes;
    private final int chunkCount;
    private final int activeConnections;

    public SegmentedDownloadElement(String name, String url, long totalBytes, long downloadedBytes, int chunkCount, int activeConnections) {
        this(UUID.randomUUID().toString(), name, url, totalBytes, downloadedBytes, chunkCount, activeConnections);
    }

    public SegmentedDownloadElement(String id, String name, String url, long totalBytes, long downloadedBytes, int chunkCount, int activeConnections) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.url = Objects.requireNonNull(url, "url must not be null");
        this.totalBytes = Math.max(0, totalBytes);
        this.downloadedBytes = Math.max(0, downloadedBytes);
        this.chunkCount = Math.max(1, chunkCount);
        this.activeConnections = Math.max(1, activeConnections);
    }

    @Override
    public void accept(DownloadVisitor visitor) {
        Objects.requireNonNull(visitor, "visitor must not be null");
        visitor.visit(this);
    }
}
