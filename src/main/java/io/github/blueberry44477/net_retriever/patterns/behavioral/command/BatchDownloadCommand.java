package io.github.blueberry44477.net_retriever.patterns.behavioral.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import lombok.Getter;

/**
 * Macro / Composite Command that groups multiple {@link DownloadCommand} instances.
 * Provides transactional all-or-nothing execution: if any command fails, all previously
 * executed commands in the batch are undone in reverse order.
 */
public class BatchDownloadCommand implements DownloadCommand {

    private final String batchName;
    private final List<DownloadCommand> commands;
    private final List<DownloadCommand> executedCommands = new ArrayList<>();

    @Getter
    private boolean executed;

    public BatchDownloadCommand(String batchName, List<DownloadCommand> commands) {
        this.batchName = Objects.requireNonNull(batchName, "batchName must not be null");
        this.commands = (commands != null) ? new ArrayList<>(commands) : new ArrayList<>();
    }

    @Override
    public void execute() throws Exception {
        System.out.printf("[BatchDownloadCommand] [Stub] execute() - Executing batch \"%s\" with %d commands%n",
                batchName, commands.size());
        executedCommands.clear();

        for (DownloadCommand command : commands) {
            try {
                command.execute();
                executedCommands.add(command);
            } catch (Exception ex) {
                System.out.printf("[BatchDownloadCommand] [Stub] Command failed in batch \"%s\": %s. Rolling back executed commands...%n",
                        batchName, ex.getMessage());
                // Rollback in reverse order
                for (int i = executedCommands.size() - 1; i >= 0; i--) {
                    DownloadCommand executedCmd = executedCommands.get(i);
                    if (executedCmd.canUndo()) {
                        try {
                            executedCmd.undo();
                        } catch (Exception rollbackEx) {
                            System.err.printf("[BatchDownloadCommand] Error during rollback of %s: %s%n",
                                    executedCmd.getName(), rollbackEx.getMessage());
                        }
                    }
                }
                executedCommands.clear();
                this.executed = false;
                throw ex;
            }
        }
        this.executed = true;
    }

    @Override
    public void undo() throws Exception {
        if (!executed) {
            return;
        }
        System.out.printf("[BatchDownloadCommand] [Stub] undo() - Reversing batch \"%s\" in reverse order%n", batchName);
        for (int i = executedCommands.size() - 1; i >= 0; i--) {
            DownloadCommand cmd = executedCommands.get(i);
            if (cmd.canUndo()) {
                cmd.undo();
            }
        }
        executedCommands.clear();
        this.executed = false;
    }

    @Override
    public boolean canUndo() {
        return commands.stream().allMatch(DownloadCommand::canUndo);
    }

    @Override
    public String getName() {
        return "BatchDownloadCommand[" + batchName + ", size=" + commands.size() + "]";
    }

    /**
     * Unmodifiable view of child commands.
     *
     * @return list of commands
     */
    public List<DownloadCommand> getCommands() {
        return Collections.unmodifiableList(commands);
    }
}
