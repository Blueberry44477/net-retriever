package io.github.blueberry44477.net_retriever.patterns.behavioral.command;

import java.util.Objects;

import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;
import lombok.Getter;

/**
 * Concrete Command that starts downloading a task.
 * Undo operation cancels or pauses the newly started task.
 */
public class StartDownloadCommand implements DownloadCommand {

    private final DownloadTaskReceiver receiver;
    private final long totalBytes;
    private DownloadStatus previousStatus;

    @Getter
    private boolean executed;

    public StartDownloadCommand(DownloadTaskReceiver receiver, long totalBytes) {
        this.receiver = Objects.requireNonNull(receiver, "receiver must not be null");
        this.totalBytes = totalBytes;
    }

    @Override
    public void execute() {
        this.previousStatus = receiver.getStatus();
        System.out.printf("[StartDownloadCommand] [Stub] execute() - Starting task \"%s\" (totalBytes=%d)%n",
                receiver.getTask().getFileName(), totalBytes);
        receiver.actionStart(totalBytes);
        this.executed = true;
    }

    @Override
    public void undo() {
        if (!executed) {
            return;
        }
        System.out.printf("[StartDownloadCommand] [Stub] undo() - Reverting start for task \"%s\"%n",
                receiver.getTask().getFileName());
        if (receiver.getStatus() == DownloadStatus.DOWNLOADING) {
            receiver.actionCancel();
        }
        this.executed = false;
    }

    @Override
    public String getName() {
        return "StartDownloadCommand[" + receiver.getTask().getFileName() + "]";
    }
}
