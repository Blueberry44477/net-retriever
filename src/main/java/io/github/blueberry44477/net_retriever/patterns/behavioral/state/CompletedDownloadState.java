package io.github.blueberry44477.net_retriever.patterns.behavioral.state;

import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;

/**
 * Concrete State representing a completed download task where all bytes were transferred
 * and verified. Terminal state: no further mutations are allowed.
 */
public class CompletedDownloadState implements DownloadState {

    @Override
    public DownloadStatus getStatus() {
        return DownloadStatus.COMPLETED;
    }

    @Override
    public String getStateName() {
        return "COMPLETED";
    }

    @Override
    public void start(DownloadTaskStateContext context, long totalBytes) {
        throw new IllegalStateException("Download is already COMPLETED. Cannot restart an existing task.");
    }

    @Override
    public void updateProgress(DownloadTaskStateContext context, long newlyDownloadedBytes) {
        throw new IllegalStateException("Download is already COMPLETED. Progress cannot be updated.");
    }

    @Override
    public void pause(DownloadTaskStateContext context) {
        throw new IllegalStateException("Cannot pause a COMPLETED download.");
    }

    @Override
    public void resume(DownloadTaskStateContext context) {
        throw new IllegalStateException("Cannot resume a COMPLETED download.");
    }

    @Override
    public void complete(DownloadTaskStateContext context) {
        throw new IllegalStateException("Download is already COMPLETED.");
    }

    @Override
    public void cancel(DownloadTaskStateContext context) {
        throw new IllegalStateException("Cannot cancel a COMPLETED download.");
    }

    @Override
    public void fail(DownloadTaskStateContext context, String errorMessage) {
        throw new IllegalStateException("Cannot fail an already COMPLETED download.");
    }

    @Override
    public void retry(DownloadTaskStateContext context) {
        throw new IllegalStateException("Cannot retry a COMPLETED download.");
    }

    @Override
    public boolean isTerminal() {
        return true;
    }

    @Override
    public boolean canResume() {
        return false;
    }
}
