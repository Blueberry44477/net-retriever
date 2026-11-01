package io.github.blueberry44477.net_retriever.patterns.behavioral.observer;

import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;

/**
 * Subject interface in the GoF Observer pattern.
 * Maintains a subscriber registry and dispatches events to registered {@link DownloadObserver} instances.
 */
public interface DownloadObservable {

    /**
     * Attaches an observer to this subject.
     *
     * @param observer subscriber to add
     */
    void addObserver(DownloadObserver observer);

    /**
     * Detaches an observer from this subject.
     *
     * @param observer subscriber to remove
     * @return true if the observer was registered and removed, false otherwise
     */
    boolean removeObserver(DownloadObserver observer);

    /**
     * Broadcasts byte streaming progress and throughput rate to all observers.
     *
     * @param downloadedBytes        cumulative downloaded bytes
     * @param transferRateBytesPerSec instantaneous transfer speed in B/s
     */
    void notifyProgress(long downloadedBytes, long transferRateBytesPerSec);

    /**
     * Broadcasts a lifecycle status change to all observers.
     *
     * @param newStatus new {@link DownloadStatus}
     * @param message   descriptive status message
     */
    void notifyStatus(DownloadStatus newStatus, String message);

    /**
     * Broadcasts a failure error event to all observers.
     *
     * @param errorMessage failure description
     */
    void notifyError(String errorMessage);

    /**
     * Returns the total count of currently registered observers.
     *
     * @return subscriber count
     */
    int getObserverCount();
}
