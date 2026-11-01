package io.github.blueberry44477.net_retriever.patterns.behavioral.mediator;

import java.util.Objects;

import io.github.blueberry44477.net_retriever.downloadtask.DownloadTask;
import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Concrete Colleague managing network streaming execution of download tasks.
 */
@Getter
@NoArgsConstructor
public class DownloaderColleague extends DownloadColleague {

    private boolean paused = false;
    private int throttleSpeedKbps = 0;
    private int activeDownloadsCount = 0;

    public DownloaderColleague(DownloadSessionMediator mediator) {
        super(mediator);
    }

    @Override
    public String getName() {
        return "DownloaderEngine";
    }

    /**
     * Initiates streaming for a download task, notifying the mediator.
     *
     * @param task       target download task
     * @param totalBytes content length in bytes
     */
    public void startDownload(DownloadTask task, long totalBytes) {
        Objects.requireNonNull(task, "DownloadTask must not be null");
        System.out.printf("[DownloaderColleague] [Stub] startDownload(task=\"%s\", bytes=%d)%n",
                task.getFileName(), totalBytes);
        task.start(totalBytes);
        this.activeDownloadsCount++;
        if (mediator != null) {
            mediator.notify(this, DownloadEvent.TASK_STARTED, task);
        }
    }

    /**
     * Reports incremental progress of received bytes, notifying the mediator.
     *
     * @param task  target task
     * @param bytes downloaded bytes
     */
    public void reportProgress(DownloadTask task, long bytes) {
        Objects.requireNonNull(task, "DownloadTask must not be null");
        System.out.printf("[DownloaderColleague] [Stub] reportProgress(task=\"%s\", bytes=%d)%n",
                task.getFileName(), bytes);
        task.updateProgress(bytes);
        if (mediator != null) {
            mediator.notify(this, DownloadEvent.CHUNK_PROGRESS, task);
        }
    }

    /**
     * Marks download task as completed, notifying the mediator.
     *
     * @param task completed task
     */
    public void completeDownload(DownloadTask task) {
        Objects.requireNonNull(task, "DownloadTask must not be null");
        System.out.printf("[DownloaderColleague] [Stub] completeDownload(task=\"%s\")%n", task.getFileName());
        task.complete();
        if (activeDownloadsCount > 0) {
            activeDownloadsCount--;
        }
        if (mediator != null) {
            mediator.notify(this, DownloadEvent.DOWNLOAD_COMPLETED, task);
        }
    }

    /**
     * Marks download task as failed due to an error, notifying the mediator.
     *
     * @param task         failed task
     * @param errorMessage reason for failure
     */
    public void failDownload(DownloadTask task, String errorMessage) {
        Objects.requireNonNull(task, "DownloadTask must not be null");
        System.out.printf("[DownloaderColleague] [Stub] failDownload(task=\"%s\", error=\"%s\")%n",
                task.getFileName(), errorMessage);
        task.fail(errorMessage);
        if (activeDownloadsCount > 0) {
            activeDownloadsCount--;
        }
        if (mediator != null) {
            mediator.notify(this, DownloadEvent.DOWNLOAD_FAILED, task);
        }
    }

    /**
     * Instructed by mediator to pause active downloads.
     */
    public void pauseActiveDownloads() {
        System.out.println("[DownloaderColleague] [Stub] pauseActiveDownloads() - Pausing all in-flight streams.");
        this.paused = true;
    }

    /**
     * Instructed by mediator to resume paused downloads.
     */
    public void resumeActiveDownloads() {
        System.out.println("[DownloaderColleague] [Stub] resumeActiveDownloads() - Resuming in-flight streams.");
        this.paused = false;
    }

    /**
     * Instructed by mediator to throttle streaming bandwidth.
     *
     * @param speedLimitKbps maximum rate in KB/s
     */
    public void throttleSpeed(int speedLimitKbps) {
        System.out.printf("[DownloaderColleague] [Stub] throttleSpeed(%d KB/s) - Applying QoS throttle.%n",
                speedLimitKbps);
        this.throttleSpeedKbps = speedLimitKbps;
    }
}
