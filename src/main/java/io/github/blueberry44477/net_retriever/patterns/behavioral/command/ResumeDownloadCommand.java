package io.github.blueberry44477.net_retriever.patterns.behavioral.command;

import java.util.Objects;

import lombok.Getter;

/**
 * Concrete Command that resumes a paused download.
 * Undo operation pauses the resumed download.
 */
public class ResumeDownloadCommand implements DownloadCommand {

    private final DownloadTaskReceiver receiver;

    @Getter
    private boolean executed;

    public ResumeDownloadCommand(DownloadTaskReceiver receiver) {
        this.receiver = Objects.requireNonNull(receiver, "receiver must not be null");
    }

    @Override
    public void execute() {
        System.out.printf("[ResumeDownloadCommand] [Stub] execute() - Resuming task \"%s\"%n",
                receiver.getTask().getFileName());
        receiver.actionResume();
        this.executed = true;
    }

    @Override
    public void undo() {
        if (!executed) {
            return;
        }
        System.out.printf("[ResumeDownloadCommand] [Stub] undo() - Pausing task \"%s\"%n",
                receiver.getTask().getFileName());
        receiver.actionPause();
        this.executed = false;
    }

    @Override
    public String getName() {
        return "ResumeDownloadCommand[" + receiver.getTask().getFileName() + "]";
    }
}
