package io.github.blueberry44477.net_retriever.patterns.behavioral.iterator;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import io.github.blueberry44477.net_retriever.downloadtask.DownloadTask;

/**
 * Concrete Iterator traversing tasks in the order of insertion into the collection.
 */
public class SequentialTaskIterator implements TaskIterator {

    private final List<DownloadTask> tasks;
    private int currentIndex = 0;

    public SequentialTaskIterator(List<DownloadTask> tasks) {
        this.tasks = new ArrayList<>(Objects.requireNonNull(tasks, "tasks must not be null"));
    }

    @Override
    public boolean hasNext() {
        return currentIndex < tasks.size();
    }

    @Override
    public DownloadTask next() {
        if (!hasNext()) {
            throw new NoSuchElementException("SequentialTaskIterator has reached the end of the collection.");
        }
        DownloadTask task = tasks.get(currentIndex++);
        System.out.printf("[SequentialTaskIterator] [Stub] next() -> index=%d, file=\"%s\"%n",
                currentIndex - 1, task.getFileName());
        return task;
    }

    @Override
    public void reset() {
        System.out.println("[SequentialTaskIterator] [Stub] reset() - Rewinding iterator position to 0.");
        this.currentIndex = 0;
    }
}
