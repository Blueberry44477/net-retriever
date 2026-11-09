package io.github.blueberry44477.net_retriever.patterns.behavioral.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import io.github.blueberry44477.net_retriever.downloadtask.DownloadTask;
import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * Context in the GoF State pattern.
 * Maintains the current {@link DownloadState} and delegates all lifecycle operations
 * to the active state object. Keeps track of download metrics, retry budgets,
 * and state transition history.
 */
@Getter
public class DownloadTaskStateContext {

    private final UUID taskId;
    private final String url;
    private final String fileName;
    private final String savePath;

    @Setter
    private long downloadedBytes;

    @Setter
    private long totalBytes;

    @Setter
    private String errorMessage;

    @Setter
    private int currentRetryAttempt;

    private final int maxRetries;

    private DownloadState state;

    private final List<StateTransitionRecord> transitionHistory = new ArrayList<>();

    public DownloadTaskStateContext(String url, String fileName, String savePath) {
        this(url, fileName, savePath, 3);
    }

    public DownloadTaskStateContext(String url, String fileName, String savePath, int maxRetries) {
        this.taskId = UUID.randomUUID();
        this.url = Objects.requireNonNull(url, "url must not be null");
        this.fileName = Objects.requireNonNull(fileName, "fileName must not be null");
        this.savePath = Objects.requireNonNull(savePath, "savePath must not be null");
        this.maxRetries = Math.max(0, maxRetries);
        this.downloadedBytes = 0L;
        this.totalBytes = 0L;
        this.currentRetryAttempt = 0;
        this.errorMessage = null;

        // Initial state is CREATED
        this.state = new CreatedDownloadState();
        recordTransition(null, DownloadStatus.CREATED, "Task initialized");
    }

    /**
     * Package-private transition method invoked by concrete states.
     *
     * @param newState target state
     * @param reason   explanatory transition description
     */
    void changeState(DownloadState newState, String reason) {
        Objects.requireNonNull(newState, "newState must not be null");
        DownloadStatus fromStatus = this.state != null ? this.state.getStatus() : null;
        this.state = newState;
        recordTransition(fromStatus, newState.getStatus(), reason);

        System.out.printf("[DownloadTaskStateContext] [State Transition] %s -> %s (task: \"%s\", reason: %s)%n",
                fromStatus, newState.getStatus(), fileName, reason);
    }

    private void recordTransition(DownloadStatus from, DownloadStatus to, String reason) {
        if (from != null) {
            transitionHistory.add(StateTransitionRecord.of(from, to, reason));
        }
    }

    // --- Delegation methods to active state ---

    public void start(long totalBytes) {
        state.start(this, totalBytes);
    }

    public void updateProgress(long newlyDownloadedBytes) {
        state.updateProgress(this, newlyDownloadedBytes);
    }

    public void pause() {
        state.pause(this);
    }

    public void resume() {
        state.resume(this);
    }

    public void complete() {
        state.complete(this);
    }

    public void cancel() {
        state.cancel(this);
    }

    public void fail(String errorMessage) {
        state.fail(this, errorMessage);
    }

    public void retry() {
        state.retry(this);
    }

    // --- Query and utility methods ---

    public DownloadStatus getStatus() {
        return state.getStatus();
    }

    public String getStateName() {
        return state.getStateName();
    }

    public boolean isTerminal() {
        return state.isTerminal();
    }

    public boolean canResume() {
        return state.canResume();
    }

    public boolean canRetry() {
        return currentRetryAttempt < maxRetries;
    }

    public void incrementRetryAttempt() {
        this.currentRetryAttempt++;
    }

    public void resetRetryAttempt() {
        this.currentRetryAttempt = 0;
    }

    public double getProgressPercentage() {
        if (this.totalBytes <= 0) {
            return 0.0;
        }
        return Math.min(100.0, ((double) this.downloadedBytes / this.totalBytes) * 100.0);
    }

    public List<StateTransitionRecord> getTransitionHistory() {
        return Collections.unmodifiableList(transitionHistory);
    }

    /**
     * Converts the context state into a domain {@link DownloadTask} entity.
     *
     * @return equivalent DownloadTask entity
     */
    public DownloadTask toDownloadTask() {
        DownloadTask task = new DownloadTask(this.url, this.fileName, this.savePath);
        if (this.totalBytes > 0) {
            try {
                task.start(this.totalBytes);
                if (this.downloadedBytes > 0) {
                    task.updateProgress(this.downloadedBytes);
                }
                if (this.getStatus() == DownloadStatus.PAUSED) {
                    task.pause();
                } else if (this.getStatus() == DownloadStatus.COMPLETED) {
                    task.complete();
                } else if (this.getStatus() == DownloadStatus.CANCELED) {
                    task.cancel();
                } else if (this.getStatus() == DownloadStatus.ERROR) {
                    task.fail(this.errorMessage);
                }
            } catch (Exception ignored) {
                // Keep default task state if strict transition fails
            }
        }
        return task;
    }
}
