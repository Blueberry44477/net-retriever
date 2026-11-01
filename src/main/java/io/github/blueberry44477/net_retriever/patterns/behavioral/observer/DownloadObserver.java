package io.github.blueberry44477.net_retriever.patterns.behavioral.observer;

/**
 * Observer interface in the GoF Observer pattern.
 * Subscribes to lifecycle, progress, and error updates published by a {@link DownloadObservable}.
 */
public interface DownloadObserver {

    /**
     * Called when periodic byte transfer progress and speed telemetry updates occur.
     *
     * @param event progress event payload
     */
    void onProgressUpdate(DownloadProgressEvent event);

    /**
     * Called when the download task transitions to a new lifecycle status (e.g. START, PAUSE, COMPLETE).
     *
     * @param event status change event payload
     */
    void onStatusChanged(DownloadProgressEvent event);

    /**
     * Called when a critical transfer error occurs.
     *
     * @param event error event payload
     */
    void onError(DownloadProgressEvent event);

    /**
     * Unique identifier or role name of this observer.
     *
     * @return observer name
     */
    String getName();
}
