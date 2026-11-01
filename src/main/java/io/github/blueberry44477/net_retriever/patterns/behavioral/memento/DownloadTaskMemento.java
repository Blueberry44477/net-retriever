package io.github.blueberry44477.net_retriever.patterns.behavioral.memento;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;
import lombok.Getter;

/**
 * Concrete Memento in the GoF Memento pattern.
 * Stores an immutable snapshot of internal execution state of {@link DownloadTaskOriginator}.
 * Provides a wide interface for state restoration inside the originator package.
 */
@Getter
public class DownloadTaskMemento implements TaskMemento {

    private final String checkpointId;
    private final String label;
    private final Instant timestamp;

    private final UUID taskId;
    private final String url;
    private final String fileName;
    private final String savePath;
    private final long downloadedBytes;
    private final Long totalBytes;
    private final DownloadStatus status;
    private final Set<Integer> completedChunkIndices;
    private final String checksum;

    public DownloadTaskMemento(String label,
                              UUID taskId,
                              String url,
                              String fileName,
                              String savePath,
                              long downloadedBytes,
                              Long totalBytes,
                              DownloadStatus status,
                              Set<Integer> completedChunkIndices,
                              String checksum) {
        this.checkpointId = UUID.randomUUID().toString();
        this.label = (label != null) ? label : "checkpoint-" + checkpointId.substring(0, 8);
        this.timestamp = Instant.now();
        this.taskId = taskId;
        this.url = url;
        this.fileName = fileName;
        this.savePath = savePath;
        this.downloadedBytes = downloadedBytes;
        this.totalBytes = totalBytes;
        this.status = status;
        this.completedChunkIndices = (completedChunkIndices != null)
                ? Collections.unmodifiableSet(new HashSet<>(completedChunkIndices))
                : Collections.emptySet();
        this.checksum = checksum;
    }

    @Override
    public String toString() {
        return "DownloadTaskMemento{" +
                "label='" + label + '\'' +
                ", timestamp=" + timestamp +
                ", downloadedBytes=" + downloadedBytes +
                ", totalBytes=" + totalBytes +
                ", status=" + status +
                ", chunks=" + completedChunkIndices.size() +
                '}';
    }
}
