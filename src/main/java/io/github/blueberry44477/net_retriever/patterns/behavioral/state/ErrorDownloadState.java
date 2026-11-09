package io.github.blueberry44477.net_retriever.patterns.behavioral.state;

import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;
import lombok.Getter;

/**
 * Concrete State representing a failed download.
 * Unlike terminal states, this state allows manual recovery via restart or retry.
 */
@Getter
public class ErrorDownloadState implements DownloadState {

    private final String reason;

    public ErrorDownloadState(String reason) {
        this.reason = reason != null ? reason : "Unknown execution error";
    }

    @Override
    public DownloadStatus getStatus() {
        return DownloadStatus.ERROR;
    }

    @Override
    public String getStateName() {
        return "ERROR";
    }

    @Override
    public void start(DownloadTaskStateContext context, long totalBytes) {
        if (totalBytes <= 0) {
            throw new IllegalArgumentException("totalBytes must be greater than zero: " + totalBytes);
        }
        context.setDownloadedBytes(0L);
        context.setTotalBytes(totalBytes);
        context.setErrorMessage(null);
        context.resetRetryAttempt();
        context.changeState(new DownloadingDownloadState(), "Restarting failed download from beginning");
    }

    @Override
    public void updateProgress(DownloadTaskStateContext context, long newlyDownloadedBytes) {
        throw new IllegalStateException("Cannot update progress in ERROR state. Restart or retry first.");
    }

    @Override
    public void pause(DownloadTaskStateContext context) {
        throw new IllegalStateException("Cannot pause a download in ERROR state.");
    }

    @Override
    public void resume(DownloadTaskStateContext context) {
        throw new IllegalStateException("Cannot resume directly from ERROR state. Call retry() or start().");
    }

    @Override
    public void complete(DownloadTaskStateContext context) {
        throw new IllegalStateException("Cannot complete a failed download.");
    }

    @Override
    public void cancel(DownloadTaskStateContext context) {
        context.changeState(new CanceledDownloadState(), "Failed download canceled by user");
    }

    @Override
    public void fail(DownloadTaskStateContext context, String errorMessage) {
        context.setErrorMessage(errorMessage);
        context.changeState(new ErrorDownloadState(errorMessage), "Error updated: " + errorMessage);
    }

    @Override
    public void retry(DownloadTaskStateContext context) {
        context.setErrorMessage(null);
        context.changeState(
                new DownloadingDownloadState(),
                "Manually retrying failed download from byte offset " + context.getDownloadedBytes()
        );
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public boolean canResume() {
        return false;
    }
}
