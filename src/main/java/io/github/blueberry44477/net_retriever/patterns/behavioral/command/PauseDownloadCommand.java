package io.github.blueberry44477.net_retriever.patterns.behavioral.command;

import java.util.Objects;

import lombok.Getter;

/**
 * Concrete Command that pauses an active download.
 * Undo operation resumes the paused download.
 */
public class PauseDownloadCommand implements DownloadCommand {

    private final DownloadTaskReceiver receiver;

    @Getter
    private boolean executed;

    public PauseDownloadCommand(DownloadTaskReceiver receiver) {
        this.receiver = Objects.requireNonNull(receiver, "receiver must not be null");
    }

    @Override
    public void execute() {
        System.out.printf("[PauseDownloadCommand] [Stub] execute() - Pausing task \"%s\"%n",
                receiver.getTask().getFileName());
        receiver.actionPause();
        this.executed = true;
    }

    @Override
    public void undo() {
        if (!executed) {
            return;
        }
        System.out.printf("[PauseDownloadCommand] [Stub] undo() - Resuming task \"%s\"%n",
                receiver.getTask().getFileName());
        receiver.actionResume();
        this.executed = false;
    }

    @Override
    public String getName() {
        return "PauseDownloadCommand[" + receiver.getTask().getFileName() + "]";
    }
}
