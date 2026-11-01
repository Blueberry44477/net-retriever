package io.github.blueberry44477.net_retriever.patterns.behavioral.command;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Invoker in the GoF Command pattern.
 * Manages command execution lifecycle, command queueing, and multi-level Undo/Redo history stacks.
 */
public class DownloadCommandInvoker {

    private final Deque<DownloadCommand> undoStack = new ArrayDeque<>();
    private final Deque<DownloadCommand> redoStack = new ArrayDeque<>();
    private final Queue<DownloadCommand> executionQueue = new LinkedList<>();
    private final List<DownloadCommand> historyLog = new ArrayList<>();

    /**
     * Executes a command immediately, recording it into the undo history if reversible.
     *
     * @param command the command to execute
     * @throws Exception if command execution fails
     */
    public void executeCommand(DownloadCommand command) throws Exception {
        System.out.printf("[DownloadCommandInvoker] [Stub] executeCommand(\"%s\")%n", command.getName());
        command.execute();
        if (command.canUndo()) {
            undoStack.push(command);
        }
        redoStack.clear();
        historyLog.add(command);
    }

    /**
     * Undoes the most recently executed reversible command from the undo stack.
     *
     * @return true if a command was undone; false if undo stack is empty
     * @throws Exception if undo operation fails
     */
    public boolean undoLast() throws Exception {
        if (undoStack.isEmpty()) {
            System.out.println("[DownloadCommandInvoker] [Stub] undoLast() - Undo stack is empty.");
            return false;
        }
        DownloadCommand command = undoStack.pop();
        System.out.printf("[DownloadCommandInvoker] [Stub] undoLast() - Reverting: \"%s\"%n", command.getName());
        command.undo();
        redoStack.push(command);
        return true;
    }

    /**
     * Redoes the most recently undone command from the redo stack.
     *
     * @return true if a command was redone; false if redo stack is empty
     * @throws Exception if redo operation fails
     */
    public boolean redoLast() throws Exception {
        if (redoStack.isEmpty()) {
            System.out.println("[DownloadCommandInvoker] [Stub] redoLast() - Redo stack is empty.");
            return false;
        }
        DownloadCommand command = redoStack.pop();
        System.out.printf("[DownloadCommandInvoker] [Stub] redoLast() - Re-executing: \"%s\"%n", command.getName());
        command.execute();
        undoStack.push(command);
        return true;
    }

    /**
     * Enqueues a command for deferred batch execution.
     *
     * @param command the command to enqueue
     */
    public void queueCommand(DownloadCommand command) {
        System.out.printf("[DownloadCommandInvoker] [Stub] queueCommand(\"%s\")%n", command.getName());
        executionQueue.offer(command);
    }

    /**
     * Drains and executes all queued commands in FIFO order.
     *
     * @return number of commands executed
     * @throws Exception if any command execution fails
     */
    public int executeQueuedCommands() throws Exception {
        int executedCount = 0;
        System.out.printf("[DownloadCommandInvoker] [Stub] executeQueuedCommands() - Processing %d queued commands%n",
                executionQueue.size());
        while (!executionQueue.isEmpty()) {
            DownloadCommand command = executionQueue.poll();
            executeCommand(command);
            executedCount++;
        }
        return executedCount;
    }

    /**
     * Clears all history stacks and queued commands.
     */
    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
        executionQueue.clear();
        historyLog.clear();
    }

    public int getUndoStackSize() {
        return undoStack.size();
    }

    public int getRedoStackSize() {
        return redoStack.size();
    }

    public int getQueueSize() {
        return executionQueue.size();
    }

    public List<DownloadCommand> getHistory() {
        return Collections.unmodifiableList(historyLog);
    }
}
