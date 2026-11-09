package io.github.blueberry44477.net_retriever.patterns.behavioral.state;

import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;

/**
 * Concrete State representing a freshly instantiated download task that has not yet started.
 */
public class CreatedDownloadState implements DownloadState {

    @Override
    public DownloadStatus getStatus() {
        return DownloadStatus.CREATED;
    }

    @Override
    public String getStateName() {
        return "CREATED";
    }

    @Override
    public void start(DownloadTaskStateContext context, long totalBytes) {
        if (totalBytes <= 0) {
            throw new IllegalArgumentException("totalBytes must be greater than zero: " + totalBytes);
        }
        context.setTotalBytes(totalBytes);
        context.setErrorMessage(null);
        context.resetRetryAttempt();
        context.changeState(new DownloadingDownloadState(), "Download started with totalBytes=" + totalBytes);
    }

    @Override
    public void updateProgress(DownloadTaskStateContext context, long newlyDownloadedBytes) {
        throw new IllegalStateException("Cannot update progress in CREATED state. Start download first.");
    }

    @Override
    public void pause(DownloadTaskStateContext context) {
        throw new IllegalStateException("Cannot pause download in CREATED state. Download has not started.");
    }

    @Override
    public void resume(DownloadTaskStateContext context) {
        throw new IllegalStateException("Cannot resume download in CREATED state. Download has not started.");
    }

    @Override
    public void complete(DownloadTaskStateContext context) {
        throw new IllegalStateException("Cannot complete download from CREATED state.");
    }

    @Override
    public void cancel(DownloadTaskStateContext context) {
        context.changeState(new CanceledDownloadState(), "Download canceled before starting");
    }

    @Override
    public void fail(DownloadTaskStateContext context, String errorMessage) {
        context.setErrorMessage(errorMessage);
        context.changeState(new ErrorDownloadState(errorMessage), "Download failed during initialization");
    }

    @Override
    public void retry(DownloadTaskStateContext context) {
        throw new IllegalStateException("Cannot retry download in CREATED state. Start download instead.");
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
