package io.github.blueberry44477.net_retriever.patterns.behavioral.observer;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;
import lombok.Getter;

/**
 * Concrete Subject in the GoF Observer pattern.
 * Manages observers for a download task and publishes progress and status transition events.
 */
@Getter
public class DownloadTaskSubject implements DownloadObservable {

    private final UUID taskId;
    private final String url;
    private final String fileName;
    private final String savePath;
    private long downloadedBytes = 0L;
    private Long totalBytes = 0L;
    private DownloadStatus status = DownloadStatus.CREATED;

    private final List<DownloadObserver> observers = new CopyOnWriteArrayList<>();

    public DownloadTaskSubject(String url, String fileName, String savePath) {
        this.taskId = UUID.randomUUID();
        this.url = Objects.requireNonNull(url, "url must not be null");
        this.fileName = Objects.requireNonNull(fileName, "fileName must not be null");
        this.savePath = Objects.requireNonNull(savePath, "savePath must not be null");
    }

    @Override
    public void addObserver(DownloadObserver observer) {
        Objects.requireNonNull(observer, "Observer must not be null");
        System.out.printf("[DownloadTaskSubject] [Stub] addObserver(\"%s\") to task \"%s\"%n",
                observer.getName(), fileName);
        observers.add(observer);
    }

    @Override
    public boolean removeObserver(DownloadObserver observer) {
        Objects.requireNonNull(observer, "Observer must not be null");
        System.out.printf("[DownloadTaskSubject] [Stub] removeObserver(\"%s\") from task \"%s\"%n",
                observer.getName(), fileName);
        return observers.remove(observer);
    }

    @Override
    public void notifyProgress(long downloadedBytes, long transferRateBytesPerSec) {
        this.downloadedBytes = downloadedBytes;
        DownloadProgressEvent event = createEvent(status, transferRateBytesPerSec, "Progress updated");
        for (DownloadObserver observer : observers) {
            try {
                observer.onProgressUpdate(event);
            } catch (Exception ex) {
                System.err.printf("[DownloadTaskSubject] Error in observer %s: %s%n",
                        observer.getName(), ex.getMessage());
            }
        }
    }

    @Override
    public void notifyStatus(DownloadStatus newStatus, String message) {
        this.status = newStatus;
        DownloadProgressEvent event = createEvent(newStatus, 0L, message);
        for (DownloadObserver observer : observers) {
            try {
                observer.onStatusChanged(event);
            } catch (Exception ex) {
                System.err.printf("[DownloadTaskSubject] Error in observer %s: %s%n",
                        observer.getName(), ex.getMessage());
            }
        }
    }

    @Override
    public void notifyError(String errorMessage) {
        this.status = DownloadStatus.ERROR;
        DownloadProgressEvent event = createEvent(DownloadStatus.ERROR, 0L, errorMessage);
        for (DownloadObserver observer : observers) {
            try {
                observer.onError(event);
            } catch (Exception ex) {
                System.err.printf("[DownloadTaskSubject] Error in observer %s: %s%n",
                        observer.getName(), ex.getMessage());
            }
        }
    }

    @Override
    public int getObserverCount() {
        return observers.size();
    }

    /**
     * Starts download streaming and broadcasts status change.
     *
     * @param totalBytes expected content length
     */
    public void start(long totalBytes) {
        System.out.printf("[DownloadTaskSubject] [Stub] start(task=\"%s\", totalBytes=%d)%n",
                fileName, totalBytes);
        this.totalBytes = totalBytes;
        notifyStatus(DownloadStatus.DOWNLOADING, "Download started");
    }

    /**
     * Streams byte chunks and broadcasts progress update.
     *
     * @param bytesDownloaded cumulative bytes downloaded
     * @param currentSpeed    instantaneous transfer speed in B/s
     */
    public void updateProgress(long bytesDownloaded, long currentSpeed) {
        System.out.printf("[DownloadTaskSubject] [Stub] updateProgress(task=\"%s\", bytes=%d, speed=%d B/s)%n",
                fileName, bytesDownloaded, currentSpeed);
        notifyProgress(bytesDownloaded, currentSpeed);
    }

    /**
     * Pauses download streaming and broadcasts status change.
     */
    public void pause() {
        System.out.printf("[DownloadTaskSubject] [Stub] pause(task=\"%s\")%n", fileName);
        notifyStatus(DownloadStatus.PAUSED, "Download paused");
    }

    /**
     * Completes download streaming and broadcasts status change.
     */
    public void complete() {
        System.out.printf("[DownloadTaskSubject] [Stub] complete(task=\"%s\")%n", fileName);
        this.downloadedBytes = (totalBytes != null && totalBytes > 0) ? totalBytes : downloadedBytes;
        notifyStatus(DownloadStatus.COMPLETED, "Download completed successfully");
    }

    /**
     * Fails download and broadcasts error event.
     *
     * @param reason error cause
     */
    public void fail(String reason) {
        System.out.printf("[DownloadTaskSubject] [Stub] fail(task=\"%s\", reason=\"%s\")%n", fileName, reason);
        notifyError(reason);
    }

    private DownloadProgressEvent createEvent(DownloadStatus status, long transferRate, String msg) {
        double percentage = (totalBytes != null && totalBytes > 0)
                ? Math.min(100.0, ((double) downloadedBytes / totalBytes) * 100.0)
                : 0.0;

        return DownloadProgressEvent.builder()
                .taskId(taskId)
                .url(url)
                .fileName(fileName)
                .downloadedBytes(downloadedBytes)
                .totalBytes(totalBytes)
                .progressPercentage(percentage)
                .transferRateBytesPerSec(transferRate)
                .status(status)
                .timestamp(Instant.now())
                .message(msg)
                .build();
    }
}
