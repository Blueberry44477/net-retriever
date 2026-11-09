package io.github.blueberry44477.net_retriever.patterns.behavioral.state;

import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;

/**
 * Concrete State representing a paused download task whose downloaded byte progress is preserved.
 */
public class PausedDownloadState implements DownloadState {

    @Override
    public DownloadStatus getStatus() {
        return DownloadStatus.PAUSED;
    }

    @Override
    public String getStateName() {
        return "PAUSED";
    }

    @Override
    public void start(DownloadTaskStateContext context, long totalBytes) {
        throw new IllegalStateException("Task is PAUSED. Call resume() instead of start().");
    }

    @Override
    public void updateProgress(DownloadTaskStateContext context, long newlyDownloadedBytes) {
        throw new IllegalStateException("Cannot update progress while task is PAUSED.");
    }

    @Override
    public void pause(DownloadTaskStateContext context) {
        throw new IllegalStateException("Download task is already PAUSED.");
    }

    @Override
    public void resume(DownloadTaskStateContext context) {
        context.changeState(
                new DownloadingDownloadState(),
                "Download resumed from byte offset " + context.getDownloadedBytes()
        );
    }

    @Override
    public void complete(DownloadTaskStateContext context) {
        throw new IllegalStateException("Cannot complete download while PAUSED. Resume first.");
    }

    @Override
    public void cancel(DownloadTaskStateContext context) {
        context.changeState(new CanceledDownloadState(), "Paused download canceled");
    }

    @Override
    public void fail(DownloadTaskStateContext context, String errorMessage) {
        context.setErrorMessage(errorMessage);
        context.changeState(new ErrorDownloadState(errorMessage), "Paused download failed: " + errorMessage);
    }

    @Override
    public void retry(DownloadTaskStateContext context) {
        throw new IllegalStateException("Cannot retry a paused task. Call resume() to continue.");
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
