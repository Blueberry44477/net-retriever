package io.github.blueberry44477.net_retriever.patterns.behavioral.visitor;

import java.util.Objects;
import java.util.UUID;

import lombok.Getter;

/**
 * Concrete Element representing a standard direct single-file download.
 */
@Getter
public class SimpleDownloadElement implements DownloadElement {

    private final String id;
    private final String name;
    private final String url;
    private final long totalBytes;
    private final long downloadedBytes;
    private final String mimeType;
    private final String sha256Checksum;

    public SimpleDownloadElement(String name, String url, long totalBytes, long downloadedBytes, String mimeType, String sha256Checksum) {
        this(UUID.randomUUID().toString(), name, url, totalBytes, downloadedBytes, mimeType, sha256Checksum);
    }

    public SimpleDownloadElement(String id, String name, String url, long totalBytes, long downloadedBytes, String mimeType, String sha256Checksum) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.url = Objects.requireNonNull(url, "url must not be null");
        this.totalBytes = Math.max(0, totalBytes);
        this.downloadedBytes = Math.max(0, downloadedBytes);
        this.mimeType = mimeType != null ? mimeType : "application/octet-stream";
        this.sha256Checksum = sha256Checksum != null ? sha256Checksum : "sha256:none";
    }

    @Override
    public void accept(DownloadVisitor visitor) {
        Objects.requireNonNull(visitor, "visitor must not be null");
        visitor.visit(this);
    }
}
