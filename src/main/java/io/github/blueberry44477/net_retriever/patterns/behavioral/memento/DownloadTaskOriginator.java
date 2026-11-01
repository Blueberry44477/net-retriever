package io.github.blueberry44477.net_retriever.patterns.behavioral.memento;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * Originator in the GoF Memento pattern.
 * Manages active download transfer lifecycle, chunk completion tracking, and creates/restores
 * execution snapshots without exposing internal mutable structures.
 */
@Getter
@Setter
public class DownloadTaskOriginator {

    private UUID taskId;
    private String url;
    private String fileName;
    private String savePath;
    private long downloadedBytes;
    private Long totalBytes;
    private DownloadStatus status;
    private Set<Integer> completedChunkIndices;
    private String checksum;

    public DownloadTaskOriginator(String url, String fileName, String savePath) {
        this.taskId = UUID.randomUUID();
        this.url = Objects.requireNonNull(url, "url must not be null");
        this.fileName = Objects.requireNonNull(fileName, "fileName must not be null");
        this.savePath = Objects.requireNonNull(savePath, "savePath must not be null");
        this.downloadedBytes = 0L;
        this.totalBytes = 0L;
        this.status = DownloadStatus.CREATED;
        this.completedChunkIndices = new HashSet<>();
        this.checksum = null;
    }

    /**
     * Starts the download transfer with an expected total size.
     *
     * @param totalBytes expected content length
     */
    public void start(long totalBytes) {
        System.out.printf("[DownloadTaskOriginator] [Stub] start(task=\"%s\", totalBytes=%d)%n",
                fileName, totalBytes);
        this.totalBytes = totalBytes;
        this.status = DownloadStatus.DOWNLOADING;
    }

    /**
     * Records byte progress and marks a downloaded chunk index as completed.
     *
     * @param newlyDownloadedBytes total cumulative downloaded bytes
     * @param chunkIndex           index of completed chunk
     */
    public void recordChunkProgress(long newlyDownloadedBytes, int chunkIndex) {
        System.out.printf("[DownloadTaskOriginator] [Stub] recordChunkProgress(task=\"%s\", bytes=%d, chunk=%d)%n",
                fileName, newlyDownloadedBytes, chunkIndex);
        this.downloadedBytes = newlyDownloadedBytes;
        this.completedChunkIndices.add(chunkIndex);
    }

    /**
     * Pauses the active download stream.
     */
    public void pause() {
        System.out.printf("[DownloadTaskOriginator] [Stub] pause(task=\"%s\") - Current bytes: %d%n",
                fileName, downloadedBytes);
        this.status = DownloadStatus.PAUSED;
    }

    /**
     * Marks the download as failed due to a network or I/O error.
     *
     * @param reason error description
     */
    public void fail(String reason) {
        System.out.printf("[DownloadTaskOriginator] [Stub] fail(task=\"%s\", reason=\"%s\")%n",
                fileName, reason);
        this.status = DownloadStatus.ERROR;
    }

    /**
     * Creates an immutable memento capturing current execution state.
     *
     * @param label human-readable milestone label
     * @return new {@link TaskMemento} snapshot
     */
    public TaskMemento saveState(String label) {
        System.out.printf("[DownloadTaskOriginator] [Stub] saveState(label=\"%s\") - Snapshotting %d bytes, %d chunks%n",
                label, downloadedBytes, completedChunkIndices.size());
        return new DownloadTaskMemento(
                label,
                taskId,
                url,
                fileName,
                savePath,
                downloadedBytes,
                totalBytes,
                status,
                completedChunkIndices,
                checksum
        );
    }

    /**
     * Restores state from a given memento snapshot.
     *
     * @param memento snapshot to restore
     */
    public void restoreState(TaskMemento memento) {
        Objects.requireNonNull(memento, "Memento must not be null");
        if (!(memento instanceof DownloadTaskMemento fullMemento)) {
            throw new IllegalArgumentException("Unsupported memento implementation: " + memento.getClass());
        }

        System.out.printf("[DownloadTaskOriginator] [Stub] restoreState(checkpoint=\"%s\", label=\"%s\") - Restoring %d bytes, %d chunks%n",
                fullMemento.getCheckpointId(), fullMemento.getLabel(), fullMemento.getDownloadedBytes(),
                fullMemento.getCompletedChunkIndices().size());

        this.taskId = fullMemento.getTaskId();
        this.url = fullMemento.getUrl();
        this.fileName = fullMemento.getFileName();
        this.savePath = fullMemento.getSavePath();
        this.downloadedBytes = fullMemento.getDownloadedBytes();
        this.totalBytes = fullMemento.getTotalBytes();
        this.status = fullMemento.getStatus();
        this.completedChunkIndices = new HashSet<>(fullMemento.getCompletedChunkIndices());
        this.checksum = fullMemento.getChecksum();
    }

    /**
     * Returns an unmodifiable view of completed chunk indices.
     *
     * @return unmodifiable set of chunk indices
     */
    public Set<Integer> getCompletedChunkIndices() {
        return Collections.unmodifiableSet(completedChunkIndices);
    }
}
