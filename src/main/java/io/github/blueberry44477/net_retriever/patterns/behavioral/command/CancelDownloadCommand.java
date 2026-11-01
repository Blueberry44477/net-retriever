package io.github.blueberry44477.net_retriever.patterns.behavioral.command;

import java.util.Objects;

import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;
import lombok.Getter;

/**
 * Concrete Command that permanently cancels an in-flight download task.
 * As a terminal state transition, this command is non-reversible (canUndo = false).
 */
public class CancelDownloadCommand implements DownloadCommand {

    private final DownloadTaskReceiver receiver;
    private DownloadStatus previousStatus;

    @Getter
    private boolean executed;

    public CancelDownloadCommand(DownloadTaskReceiver receiver) {
        this.receiver = Objects.requireNonNull(receiver, "receiver must not be null");
    }

    @Override
    public void execute() {
        this.previousStatus = receiver.getStatus();
        System.out.printf("[CancelDownloadCommand] [Stub] execute() - Canceling task \"%s\" (previousStatus=%s)%n",
                receiver.getTask().getFileName(), previousStatus);
        receiver.actionCancel();
        this.executed = true;
    }

    @Override
    public void undo() {
        throw new UnsupportedOperationException(
                "CancelDownloadCommand is irreversible for task: " + receiver.getTask().getFileName());
    }

    @Override
    public boolean canUndo() {
        return false;
    }

    @Override
    public String getName() {
        return "CancelDownloadCommand[" + receiver.getTask().getFileName() + "]";
    }
}
