package io.github.blueberry44477.net_retriever.patterns.behavioral.iterator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import io.github.blueberry44477.net_retriever.downloadtask.DownloadTask;

/**
 * Concrete Iterator traversing tasks in a prioritized order defined by a {@link Comparator}.
 * Traversal is executed on an internal prioritized view without mutating the underlying aggregate collection.
 */
public class PriorityTaskIterator implements TaskIterator {

    /**
     * Default priority comparator: orders tasks with higher total byte size first.
     */
    public static final Comparator<DownloadTask> DEFAULT_PRIORITY_COMPARATOR = (t1, t2) -> {
        long s1 = (t1 != null && t1.getTotalBytes() != null) ? t1.getTotalBytes() : 0L;
        long s2 = (t2 != null && t2.getTotalBytes() != null) ? t2.getTotalBytes() : 0L;
        return Long.compare(s2, s1); // descending
    };

    private final List<DownloadTask> sourceTasks;
    private final Comparator<DownloadTask> comparator;
    private final List<DownloadTask> prioritizedTasks = new ArrayList<>();
    private int currentIndex = 0;

    public PriorityTaskIterator(List<DownloadTask> sourceTasks) {
        this(sourceTasks, DEFAULT_PRIORITY_COMPARATOR);
    }

    public PriorityTaskIterator(List<DownloadTask> sourceTasks, Comparator<DownloadTask> comparator) {
        this.sourceTasks = Objects.requireNonNull(sourceTasks, "sourceTasks must not be null");
        this.comparator = Objects.requireNonNull(comparator, "comparator must not be null");
        refreshAndSort();
    }

    private void refreshAndSort() {
        prioritizedTasks.clear();
        prioritizedTasks.addAll(sourceTasks);
        prioritizedTasks.sort(comparator);
    }

    @Override
    public boolean hasNext() {
        return currentIndex < prioritizedTasks.size();
    }

    @Override
    public DownloadTask next() {
        if (!hasNext()) {
            throw new NoSuchElementException("PriorityTaskIterator has reached the end of prioritized tasks.");
        }
        DownloadTask task = prioritizedTasks.get(currentIndex++);
        System.out.printf("[PriorityTaskIterator] [Stub] next() -> index=%d, file=\"%s\", totalBytes=%d%n",
                currentIndex - 1, task.getFileName(), task.getTotalBytes());
        return task;
    }

    @Override
    public void reset() {
        System.out.println("[PriorityTaskIterator] [Stub] reset() - Re-sorting prioritized tasks and rewinding position.");
        refreshAndSort();
        this.currentIndex = 0;
    }

    public int getTaskCount() {
        return prioritizedTasks.size();
    }
}
