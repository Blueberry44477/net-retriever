package io.github.blueberry44477.net_retriever.patterns.behavioral.state;

import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;

/**
 * State interface in the GoF State pattern.
 * Encapsulates state-specific behavior and lifecycle transitions for a download task.
 * Operations that are invalid in a given state throw {@link IllegalStateException}.
 */
public interface DownloadState {

    /**
     * Returns the corresponding domain {@link DownloadStatus} enum for this state.
     *
     * @return download status
     */
    DownloadStatus getStatus();

    /**
     * Returns a human-readable display name of the state.
     *
     * @return state name
     */
    String getStateName();

    /**
     * Initiates the download operation, allocating target total bytes and transitioning
     * the context into the downloading state.
     *
     * @param context    the download task context
     * @param totalBytes expected total byte size of the payload
     * @throws IllegalStateException if the task cannot be started from this state
     */
    void start(DownloadTaskStateContext context, long totalBytes);

    /**
     * Updates the byte progress of an active download.
     *
     * @param context               the download task context
     * @param newlyDownloadedBytes total accumulated bytes downloaded so far
     * @throws IllegalStateException    if progress cannot be updated in this state
     * @throws IllegalArgumentException if byte progress regresses or exceeds total bytes
     */
    void updateProgress(DownloadTaskStateContext context, long newlyDownloadedBytes);

    /**
     * Pauses an active download, preserving downloaded progress for subsequent resumption.
     *
     * @param context the download task context
     * @throws IllegalStateException if the task cannot be paused from this state
     */
    void pause(DownloadTaskStateContext context);

    /**
     * Resumes a paused or retrying download back into the active downloading state.
     *
     * @param context the download task context
     * @throws IllegalStateException if the task cannot be resumed from this state
     */
    void resume(DownloadTaskStateContext context);

    /**
     * Completes the download after all bytes have been successfully transferred and verified.
     *
     * @param context the download task context
     * @throws IllegalStateException if the task cannot be completed from this state
     */
    void complete(DownloadTaskStateContext context);

    /**
     * Cancels the download task, releasing resources and moving into terminal cancellation.
     *
     * @param context the download task context
     * @throws IllegalStateException if the task is already completed
     */
    void cancel(DownloadTaskStateContext context);

    /**
     * Handles an unrecoverable or transient error during download execution.
     * If the context supports automatic retry, transitions into retrying state;
     * otherwise transitions into error state.
     *
     * @param context      the download task context
     * @param errorMessage descriptive failure reason
     */
    void fail(DownloadTaskStateContext context, String errorMessage);

    /**
     * Triggers a retry attempt from an error or transient state.
     *
     * @param context the download task context
     * @throws IllegalStateException if retry is not permissible from this state
     */
    void retry(DownloadTaskStateContext context);

    /**
     * Indicates whether this state is terminal (i.e. no further lifecycle actions are allowed).
     *
     * @return true if state is terminal (e.g. COMPLETED or CANCELED)
     */
    boolean isTerminal();

    /**
     * Indicates whether download transfer can be resumed from this state.
     *
     * @return true if download can be resumed
     */
    boolean canResume();
}
