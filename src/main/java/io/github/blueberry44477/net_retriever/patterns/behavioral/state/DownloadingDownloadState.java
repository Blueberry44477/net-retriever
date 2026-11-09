package io.github.blueberry44477.net_retriever.patterns.behavioral.state;

import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;

/**
 * Concrete State representing an active network byte streaming operation.
 */
public class DownloadingDownloadState implements DownloadState {

    @Override
    public DownloadStatus getStatus() {
        return DownloadStatus.DOWNLOADING;
    }

    @Override
    public String getStateName() {
        return "DOWNLOADING";
    }

    @Override
    public void start(DownloadTaskStateContext context, long totalBytes) {
        throw new IllegalStateException("Download is already running in DOWNLOADING state.");
    }

    @Override
    public void updateProgress(DownloadTaskStateContext context, long newlyDownloadedBytes) {
        if (newlyDownloadedBytes < context.getDownloadedBytes()) {
            throw new IllegalArgumentException(String.format(
                    "Downloaded bytes cannot regress from %d to %d",
                    context.getDownloadedBytes(), newlyDownloadedBytes
            ));
        }
        if (context.getTotalBytes() > 0 && newlyDownloadedBytes > context.getTotalBytes()) {
            throw new IllegalArgumentException(String.format(
                    "Downloaded bytes (%d) cannot exceed total bytes (%d)",
                    newlyDownloadedBytes, context.getTotalBytes()
            ));
        }
        context.setDownloadedBytes(newlyDownloadedBytes);
    }

    @Override
    public void pause(DownloadTaskStateContext context) {
        context.changeState(new PausedDownloadState(), "Download paused by user/system");
    }

    @Override
    public void resume(DownloadTaskStateContext context) {
        throw new IllegalStateException("Cannot resume download; task is already actively DOWNLOADING.");
    }

    @Override
    public void complete(DownloadTaskStateContext context) {
        if (context.getTotalBytes() > 0) {
            context.setDownloadedBytes(context.getTotalBytes());
        }
        context.resetRetryAttempt();
        context.changeState(new CompletedDownloadState(), "Transfer finished and verified successfully");
    }

    @Override
    public void cancel(DownloadTaskStateContext context) {
        context.changeState(new CanceledDownloadState(), "Download canceled while in flight");
    }

    @Override
    public void fail(DownloadTaskStateContext context, String errorMessage) {
        context.setErrorMessage(errorMessage);
        if (context.canRetry()) {
            context.changeState(
                    new RetryingDownloadState(errorMessage),
                    "Failure encountered (" + errorMessage + "). Retrying (attempt "
                            + (context.getCurrentRetryAttempt() + 1) + "/" + context.getMaxRetries() + ")"
            );
        } else {
            context.changeState(
                    new ErrorDownloadState(errorMessage),
                    "Failure encountered (" + errorMessage + "). Retry budget exhausted"
            );
        }
    }

    @Override
    public void retry(DownloadTaskStateContext context) {
        throw new IllegalStateException("Cannot retry an active download. Wait for failure or pause first.");
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
