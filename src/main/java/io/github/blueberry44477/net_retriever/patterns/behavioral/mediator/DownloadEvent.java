package io.github.blueberry44477.net_retriever.patterns.behavioral.mediator;

/**
 * Domain events communicated between Colleagues and the Mediator in the GoF Mediator pattern.
 */
public enum DownloadEvent {

    /**
     * Download task execution has been initiated.
     */
    TASK_STARTED,

    /**
     * Chunk data transfer progress occurred.
     */
    CHUNK_PROGRESS,

    /**
     * Download task completed file transfer successfully.
     */
    DOWNLOAD_COMPLETED,

    /**
     * Download task encountered a critical failure.
     */
    DOWNLOAD_FAILED,

    /**
     * Storage monitor detected available disk space below critical threshold.
     */
    DISK_SPACE_LOW,

    /**
     * Network bandwidth demand exceeds total available bandwidth capacity.
     */
    BANDWIDTH_EXCEEDED,

    /**
     * Active download stream has been paused.
     */
    DOWNLOAD_PAUSED,

    /**
     * Paused download stream has been resumed.
     */
    DOWNLOAD_RESUMED
}
