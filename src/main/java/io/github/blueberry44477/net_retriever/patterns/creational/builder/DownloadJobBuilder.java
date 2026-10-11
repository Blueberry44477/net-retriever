package io.github.blueberry44477.net_retriever.patterns.creational.builder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Builder pattern: provides a fluent API and validation for constructing {@link DownloadJob} instances.
 */
public class DownloadJobBuilder {

    String url;
    String destinationPath = "./downloads";
    String fileName;
    DownloadPriority priority = DownloadPriority.MEDIUM;
    long speedLimitBytesPerSec = 0L;
    int chunkCount = 1;
    int maxRetries = 3;
    int timeoutSeconds = 30;
    String expectedChecksum;
    String checksumAlgorithm;
    boolean resumeSupported = true;
    Map<String, String> customHeaders = new HashMap<>();
    Set<String> tags = new HashSet<>();

    public DownloadJobBuilder() {
    }

    public DownloadJobBuilder setUrl(String url) {
        System.out.println("[DownloadJobBuilder] [Step] setUrl(\"" + url + "\")");
        this.url = url;
        return this;
    }

    public DownloadJobBuilder setDestinationPath(String destinationPath) {
        System.out.println("[DownloadJobBuilder] [Step] setDestinationPath(\"" + destinationPath + "\")");
        this.destinationPath = destinationPath;
        return this;
    }

    public DownloadJobBuilder setFileName(String fileName) {
        System.out.println("[DownloadJobBuilder] [Step] setFileName(\"" + fileName + "\")");
        this.fileName = fileName;
        return this;
    }

    public DownloadJobBuilder setPriority(DownloadPriority priority) {
        System.out.println("[DownloadJobBuilder] [Step] setPriority(" + priority + ")");
        this.priority = priority;
        return this;
    }

    public DownloadJobBuilder setSpeedLimitBytesPerSec(long speedLimitBytesPerSec) {
        System.out.println("[DownloadJobBuilder] [Step] setSpeedLimitBytesPerSec(" + speedLimitBytesPerSec + ")");
        this.speedLimitBytesPerSec = speedLimitBytesPerSec;
        return this;
    }

    public DownloadJobBuilder setChunkCount(int chunkCount) {
        System.out.println("[DownloadJobBuilder] [Step] setChunkCount(" + chunkCount + ")");
        this.chunkCount = chunkCount;
        return this;
    }

    public DownloadJobBuilder setMaxRetries(int maxRetries) {
        System.out.println("[DownloadJobBuilder] [Step] setMaxRetries(" + maxRetries + ")");
        this.maxRetries = maxRetries;
        return this;
    }

    public DownloadJobBuilder setTimeoutSeconds(int timeoutSeconds) {
        System.out.println("[DownloadJobBuilder] [Step] setTimeoutSeconds(" + timeoutSeconds + ")");
        this.timeoutSeconds = timeoutSeconds;
        return this;
    }

    public DownloadJobBuilder setChecksum(String expectedChecksum, String algorithm) {
        System.out.println("[DownloadJobBuilder] [Step] setChecksum(\"" + expectedChecksum + "\", \"" + algorithm + "\")");
        this.expectedChecksum = expectedChecksum;
        this.checksumAlgorithm = algorithm;
        return this;
    }

    public DownloadJobBuilder setResumeSupported(boolean resumeSupported) {
        System.out.println("[DownloadJobBuilder] [Step] setResumeSupported(" + resumeSupported + ")");
        this.resumeSupported = resumeSupported;
        return this;
    }

    public DownloadJobBuilder addHeader(String key, String value) {
        System.out.println("[DownloadJobBuilder] [Step] addHeader(\"" + key + "\", \"" + value + "\")");
        this.customHeaders.put(key, value);
        return this;
    }

    public DownloadJobBuilder addTag(String tag) {
        System.out.println("[DownloadJobBuilder] [Step] addTag(\"" + tag + "\")");
        this.tags.add(tag);
        return this;
    }

    /**
     * Validates configuration parameters and instantiates the immutable {@link DownloadJob}.
     */
    public DownloadJob build() {
        System.out.println("[DownloadJobBuilder] build() - Validating and constructing DownloadJob");
        if (this.url == null || this.url.isBlank()) {
            throw new IllegalStateException("URL parameter is required to build a DownloadJob");
        }
        if (this.chunkCount < 1) {
            throw new IllegalStateException("chunkCount must be at least 1");
        }
        if (this.fileName == null || this.fileName.isBlank()) {
            int lastSlash = this.url.lastIndexOf('/');
            if (lastSlash != -1 && lastSlash < this.url.length() - 1) {
                this.fileName = this.url.substring(lastSlash + 1);
            } else {
                this.fileName = "downloaded_file_" + System.currentTimeMillis();
            }
        }
        return new DownloadJob(this);
    }
}
