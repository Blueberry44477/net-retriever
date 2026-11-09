package io.github.blueberry44477.net_retriever.patterns.behavioral.state;

import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;
import lombok.Getter;

/**
 * Concrete State representing a transient retry/reconnection backoff phase.
 * Enables fault tolerance and automatic recovery without leaving the downloading lifecycle.
 */
@Getter
public class RetryingDownloadState implements DownloadState {

    private final String lastError;

    public RetryingDownloadState(String lastError) {
        this.lastError = lastError != null ? lastError : "Transient network error";
    }

    @Override
    public DownloadStatus getStatus() {
        return DownloadStatus.DOWNLOADING;
    }

    @Override
    public String getStateName() {
        return "RETRYING";
    }

    @Override
    public void start(DownloadTaskStateContext context, long totalBytes) {
        throw new IllegalStateException("Task is in RETRYING state. Call retry() or resume() to reconnect.");
    }

    @Override
    public void updateProgress(DownloadTaskStateContext context, long newlyDownloadedBytes) {
        throw new IllegalStateException("Cannot update progress while in transient RETRYING state.");
    }

    @Override
    public void pause(DownloadTaskStateContext context) {
        context.changeState(new PausedDownloadState(), "Retrying download paused by user");
    }

    @Override
    public void resume(DownloadTaskStateContext context) {
        retry(context);
    }

    @Override
    public void complete(DownloadTaskStateContext context) {
        throw new IllegalStateException("Cannot complete download while in RETRYING state.");
    }

    @Override
    public void cancel(DownloadTaskStateContext context) {
        context.changeState(new CanceledDownloadState(), "Retrying download canceled");
    }

    @Override
    public void fail(DownloadTaskStateContext context, String errorMessage) {
        context.setErrorMessage(errorMessage);
        context.incrementRetryAttempt();
        if (context.canRetry()) {
            context.changeState(
                    new RetryingDownloadState(errorMessage),
                    String.format("Retry attempt failed (%s). Remaining attempts: %d",
                            errorMessage, (context.getMaxRetries() - context.getCurrentRetryAttempt()))
            );
        } else {
            context.changeState(
                    new ErrorDownloadState(errorMessage),
                    "All retry attempts exhausted (" + context.getMaxRetries() + "). Transitioning to permanent ERROR."
            );
        }
    }

    @Override
    public void retry(DownloadTaskStateContext context) {
        context.incrementRetryAttempt();
        context.changeState(
                new DownloadingDownloadState(),
                String.format("Executing retry attempt %d of %d from byte offset %d",
                        context.getCurrentRetryAttempt(), context.getMaxRetries(), context.getDownloadedBytes())
        );
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public boolean canResume() {
        return true;
    }
}
