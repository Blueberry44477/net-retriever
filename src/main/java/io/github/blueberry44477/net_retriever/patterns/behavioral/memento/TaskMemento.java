package io.github.blueberry44477.net_retriever.patterns.behavioral.memento;

import java.time.Instant;

import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;

/**
 * Narrow Memento interface in the GoF Memento pattern.
 * Exposes only safe metadata to Caretaker components without revealing internal snapshot details.
 */
public interface TaskMemento {

    /**
     * Unique identifier for this checkpoint.
     *
     * @return checkpoint UUID/ID
     */
    String getCheckpointId();

    /**
     * Descriptive label or milestone name of this checkpoint.
     *
     * @return checkpoint label
     */
    String getLabel();

    /**
     * Point in time when this snapshot was captured.
     *
     * @return creation timestamp
     */
    Instant getTimestamp();

    /**
     * Total bytes downloaded at the time of checkpointing.
     *
     * @return downloaded byte count
     */
    long getDownloadedBytes();

    /**
     * Status of the task at the time of checkpointing.
     *
     * @return {@link DownloadStatus}
     */
    DownloadStatus getStatus();
}
