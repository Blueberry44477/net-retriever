package io.github.blueberry44477.net_retriever.patterns.behavioral.command;

/**
 * Command interface in the GoF Command pattern.
 * Encapsulates a download action, its arguments, and its reverse operation (undo).
 */
public interface DownloadCommand {

    /**
     * Executes the encapsulated operation on the target receiver.
     *
     * @throws Exception if execution fails
     */
    void execute() throws Exception;

    /**
     * Reverses the operation, returning the receiver to its previous state.
     *
     * @throws Exception if undoing fails
     */
    void undo() throws Exception;

    /**
     * Returns a human-readable identifier for this command.
     *
     * @return command description name
     */
    String getName();

    /**
     * Checks if this command has been executed and not yet undone.
     *
     * @return true if executed, false otherwise
     */
    boolean isExecuted();

    /**
     * Indicates whether this command supports undo operations.
     *
     * @return true by default; false for irreversible actions
     */
    default boolean canUndo() {
        return true;
    }
}
