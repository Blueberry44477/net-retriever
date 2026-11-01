package io.github.blueberry44477.net_retriever.patterns.behavioral.command;

import java.util.Objects;

import io.github.blueberry44477.net_retriever.downloadtask.DownloadTask;
import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;
import lombok.Getter;

/**
 * Receiver in the GoF Command pattern.
 * Performs the concrete business and lifecycle operations on an underlying {@link DownloadTask}.
 */
public class DownloadTaskReceiver {

    @Getter
    private final DownloadTask task;

    public DownloadTaskReceiver(DownloadTask task) {
        this.task = Objects.requireNonNull(task, "DownloadTask must not be null");
    }

    /**
     * Initiates the download operation for the task.
     *
     * @param totalBytes expected content length in bytes
     */
    public void actionStart(long totalBytes) {
        System.out.printf("[DownloadTaskReceiver] [Stub] actionStart(task=\"%s\", totalBytes=%d)%n",
                task.getFileName(), totalBytes);
        task.start(totalBytes);
    }

    /**
     * Pauses the active download stream.
     */
    public void actionPause() {
        System.out.printf("[DownloadTaskReceiver] [Stub] actionPause(task=\"%s\")%n", task.getFileName());
        task.pause();
    }

    /**
     * Resumes the paused download stream.
     */
    public void actionResume() {
        System.out.printf("[DownloadTaskReceiver] [Stub] actionResume(task=\"%s\")%n", task.getFileName());
        task.start(task.getTotalBytes() != null ? task.getTotalBytes() : 0L);
    }

    /**
     * Cancels the active or paused download.
     */
    public void actionCancel() {
        System.out.printf("[DownloadTaskReceiver] [Stub] actionCancel(task=\"%s\")%n", task.getFileName());
        task.cancel();
    }

    /**
     * Marks the download as completed.
     */
    public void actionComplete() {
        System.out.printf("[DownloadTaskReceiver] [Stub] actionComplete(task=\"%s\")%n", task.getFileName());
        task.complete();
    }

    /**
     * Retrieves current task status.
     *
     * @return {@link DownloadStatus}
     */
    public DownloadStatus getStatus() {
        return task.getStatus();
    }
}
