package io.github.blueberry44477.net_retriever.patterns.behavioral.visitor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import lombok.Getter;

/**
 * Concrete Element representing a resilient download backed by redundant mirror servers.
 */
@Getter
public class MirroredDownloadElement implements DownloadElement {

    private final String id;
    private final String name;
    private final String primaryUrl;
    private final List<String> mirrorUrls;
    private final String activeMirrorUrl;
    private final long totalBytes;
    private final long downloadedBytes;
    private final int failoverCount;

    public MirroredDownloadElement(String name, String primaryUrl, List<String> mirrorUrls, long totalBytes, long downloadedBytes) {
        this(UUID.randomUUID().toString(), name, primaryUrl, mirrorUrls, primaryUrl, totalBytes, downloadedBytes, 0);
    }

    public MirroredDownloadElement(
            String id,
            String name,
            String primaryUrl,
            List<String> mirrorUrls,
            String activeMirrorUrl,
            long totalBytes,
            long downloadedBytes,
            int failoverCount
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.primaryUrl = Objects.requireNonNull(primaryUrl, "primaryUrl must not be null");
        this.mirrorUrls = mirrorUrls != null ? List.copyOf(mirrorUrls) : Collections.emptyList();
        this.activeMirrorUrl = activeMirrorUrl != null ? activeMirrorUrl : primaryUrl;
        this.totalBytes = Math.max(0, totalBytes);
        this.downloadedBytes = Math.max(0, downloadedBytes);
        this.failoverCount = Math.max(0, failoverCount);
    }

    @Override
    public void accept(DownloadVisitor visitor) {
        Objects.requireNonNull(visitor, "visitor must not be null");
        visitor.visit(this);
    }
}
