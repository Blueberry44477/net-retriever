package io.github.blueberry44477.net_retriever.patterns.behavioral.memento;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Caretaker in the GoF Memento pattern.
 * Manages memento snapshots, providing sequential undo/redo stacks and named checkpoint lookup
 * while strictly adhering to encapsulation by never inspecting or modifying memento internals.
 */
public class DownloadCheckpointCaretaker {

    private final Deque<TaskMemento> undoStack = new ArrayDeque<>();
    private final Deque<TaskMemento> redoQueue = new ArrayDeque<>();
    private final Map<String, TaskMemento> namedCheckpoints = new HashMap<>();
    private final List<TaskMemento> checkpointLog = new ArrayList<>();

    /**
     * Stores a new checkpoint memento onto the undo stack.
     *
     * @param memento snapshot to safeguard
     */
    public void pushCheckpoint(TaskMemento memento) {
        Objects.requireNonNull(memento, "Memento must not be null");
        System.out.printf("[DownloadCheckpointCaretaker] [Stub] pushCheckpoint(label=\"%s\", bytes=%d)%n",
                memento.getLabel(), memento.getDownloadedBytes());
        undoStack.push(memento);
        redoQueue.clear();
        checkpointLog.add(memento);
    }

    /**
     * Pops and returns the most recent checkpoint memento for state rollback.
     *
     * @return optional containing the latest snapshot, or empty if stack is empty
     */
    public Optional<TaskMemento> popCheckpoint() {
        if (undoStack.isEmpty()) {
            System.out.println("[DownloadCheckpointCaretaker] [Stub] popCheckpoint() - Undo stack is empty.");
            return Optional.empty();
        }
        TaskMemento memento = undoStack.pop();
        System.out.printf("[DownloadCheckpointCaretaker] [Stub] popCheckpoint() -> label=\"%s\"%n", memento.getLabel());
        redoQueue.addLast(memento);
        return Optional.of(memento);
    }

    /**
     * Re-applies the most recently popped checkpoint (Redo).
     *
     * @return optional containing redo snapshot, or empty if redo stack is empty
     */
    public Optional<TaskMemento> redoCheckpoint() {
        if (redoQueue.isEmpty()) {
            System.out.println("[DownloadCheckpointCaretaker] [Stub] redoCheckpoint() - Redo queue is empty.");
            return Optional.empty();
        }
        TaskMemento memento = redoQueue.pollFirst();
        System.out.printf("[DownloadCheckpointCaretaker] [Stub] redoCheckpoint() -> label=\"%s\"%n", memento.getLabel());
        undoStack.push(memento);
        return Optional.of(memento);
    }

    /**
     * Saves a snapshot under a descriptive milestone name.
     *
     * @param name    unique checkpoint alias
     * @param memento snapshot to save
     */
    public void saveNamedCheckpoint(String name, TaskMemento memento) {
        Objects.requireNonNull(name, "Checkpoint name must not be null");
        Objects.requireNonNull(memento, "Memento must not be null");
        System.out.printf("[DownloadCheckpointCaretaker] [Stub] saveNamedCheckpoint(name=\"%s\", label=\"%s\")%n",
                name, memento.getLabel());
        namedCheckpoints.put(name, memento);
        checkpointLog.add(memento);
    }

    /**
     * Retrieves a named snapshot by its alias.
     *
     * @param name checkpoint alias
     * @return optional containing memento, or empty if not found
     */
    public Optional<TaskMemento> getNamedCheckpoint(String name) {
        return Optional.ofNullable(namedCheckpoints.get(name));
    }

    /**
     * Returns total number of checkpoints currently stored in the undo stack.
     *
     * @return stack size
     */
    public int getHistorySize() {
        return undoStack.size();
    }

    /**
     * Returns total number of checkpoints available for redo.
     *
     * @return redo queue size
     */
    public int getRedoSize() {
        return redoQueue.size();
    }

    /**
     * Returns an unmodifiable chronological log of all captured checkpoints.
     *
     * @return list of checkpoints
     */
    public List<TaskMemento> getCheckpointLog() {
        return Collections.unmodifiableList(checkpointLog);
    }

    /**
     * Clears all stored checkpoints.
     */
    public void clearHistory() {
        undoStack.clear();
        redoQueue.clear();
        namedCheckpoints.clear();
        checkpointLog.clear();
    }
}
