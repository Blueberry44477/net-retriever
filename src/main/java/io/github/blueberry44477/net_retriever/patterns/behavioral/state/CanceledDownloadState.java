package io.github.blueberry44477.net_retriever.patterns.behavioral.state;

import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;

/**
 * Concrete State representing a canceled download task.
 * Terminal state: aborted tasks cannot transition back into execution.
 */
public class CanceledDownloadState implements DownloadState {

    @Override
    public DownloadStatus getStatus() {
        return DownloadStatus.CANCELED;
    }

    @Override
    public String getStateName() {
        return "CANCELED";
    }

    @Override
    public void start(DownloadTaskStateContext context, long totalBytes) {
        throw new IllegalStateException("Download has been CANCELED. Cannot restart an aborted task.");
    }

    @Override
    public void updateProgress(DownloadTaskStateContext context, long newlyDownloadedBytes) {
        throw new IllegalStateException("Download has been CANCELED. Progress cannot be updated.");
    }

    @Override
    public void pause(DownloadTaskStateContext context) {
        throw new IllegalStateException("Cannot pause a CANCELED download.");
    }

    @Override
    public void resume(DownloadTaskStateContext context) {
        throw new IllegalStateException("Cannot resume a CANCELED download.");
    }

    @Override
    public void complete(DownloadTaskStateContext context) {
        throw new IllegalStateException("Cannot complete a CANCELED download.");
    }

    @Override
    public void cancel(DownloadTaskStateContext context) {
        throw new IllegalStateException("Download is already CANCELED.");
    }

    @Override
    public void fail(DownloadTaskStateContext context, String errorMessage) {
        throw new IllegalStateException("Cannot fail an already CANCELED download.");
    }

    @Override
    public void retry(DownloadTaskStateContext context) {
        throw new IllegalStateException("Cannot retry a CANCELED download.");
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
