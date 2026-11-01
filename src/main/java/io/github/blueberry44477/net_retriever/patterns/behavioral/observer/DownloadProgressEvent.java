package io.github.blueberry44477.net_retriever.patterns.behavioral.observer;

import java.time.Instant;
import java.util.UUID;

import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * Immutable event payload published by {@link DownloadObservable} subjects to {@link DownloadObserver} subscribers.
 */
@Getter
@Builder
public class DownloadProgressEvent {

    private final UUID taskId;
    private final String url;
    private final String fileName;
    private final long downloadedBytes;
    private final Long totalBytes;
    private final double progressPercentage;
    private final long transferRateBytesPerSec;
    private final DownloadStatus status;
    private final Instant timestamp;
    private final String message;

    @Override
    public String toString() {
        return String.format("DownloadProgressEvent[file=\"%s\", %.1f%% (%d/%d B) @ %d B/s, status=%s, msg=\"%s\"]",
                fileName, progressPercentage, downloadedBytes, totalBytes, transferRateBytesPerSec, status, message);
    }
}
