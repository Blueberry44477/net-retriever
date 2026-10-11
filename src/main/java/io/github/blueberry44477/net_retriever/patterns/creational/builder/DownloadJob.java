package io.github.blueberry44477.net_retriever.patterns.creational.builder;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import lombok.ToString;

/**
 * Builder pattern: immutable complex Product representing a configured download job.
 */
@Getter
@ToString
public final class DownloadJob {

    private final UUID id;
    private final String url;
    private final String destinationPath;
    private final String fileName;
    private final DownloadPriority priority;
    private final long speedLimitBytesPerSec;
    private final int chunkCount;
    private final int maxRetries;
    private final int timeoutSeconds;
    private final String expectedChecksum;
    private final String checksumAlgorithm;
    private final boolean resumeSupported;
    private final Map<String, String> customHeaders;
    private final Set<String> tags;

    DownloadJob(DownloadJobBuilder builder) {
        this.id = UUID.randomUUID();
        this.url = builder.url;
        this.destinationPath = builder.destinationPath;
        this.fileName = builder.fileName;
        this.priority = builder.priority;
        this.speedLimitBytesPerSec = builder.speedLimitBytesPerSec;
        this.chunkCount = builder.chunkCount;
        this.maxRetries = builder.maxRetries;
        this.timeoutSeconds = builder.timeoutSeconds;
        this.expectedChecksum = builder.expectedChecksum;
        this.checksumAlgorithm = builder.checksumAlgorithm;
        this.resumeSupported = builder.resumeSupported;
        this.customHeaders = Collections.unmodifiableMap(builder.customHeaders);
        this.tags = Collections.unmodifiableSet(builder.tags);
    }

    public void start() {
        System.out.println("[DownloadJob] [Stub] start() - id=" + id
                + ", url=" + url + ", target=" + destinationPath + "/" + fileName
                + ", chunks=" + chunkCount + ", priority=" + priority);
    }
}
