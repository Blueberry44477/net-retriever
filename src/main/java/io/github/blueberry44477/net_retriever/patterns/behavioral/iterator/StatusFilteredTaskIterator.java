package io.github.blueberry44477.net_retriever.patterns.behavioral.iterator;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;

import io.github.blueberry44477.net_retriever.downloadtask.DownloadTask;
import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;
import lombok.Getter;

/**
 * Concrete Iterator traversing only tasks that satisfy a given {@link DownloadStatus} or filter predicate.
 */
public class StatusFilteredTaskIterator implements TaskIterator {

    private final List<DownloadTask> sourceTasks;
    private final Predicate<DownloadTask> filterPredicate;
    @Getter
    private final DownloadStatus targetStatus;
    private final List<DownloadTask> filteredTasks = new ArrayList<>();
    private int currentIndex = 0;

    public StatusFilteredTaskIterator(List<DownloadTask> sourceTasks, DownloadStatus targetStatus) {
        this.sourceTasks = Objects.requireNonNull(sourceTasks, "sourceTasks must not be null");
        this.targetStatus = Objects.requireNonNull(targetStatus, "targetStatus must not be null");
        this.filterPredicate = task -> task != null && task.getStatus() == targetStatus;
        applyFilter();
    }

    public StatusFilteredTaskIterator(List<DownloadTask> sourceTasks, Predicate<DownloadTask> filterPredicate) {
        this.sourceTasks = Objects.requireNonNull(sourceTasks, "sourceTasks must not be null");
        this.filterPredicate = Objects.requireNonNull(filterPredicate, "filterPredicate must not be null");
        this.targetStatus = null;
        applyFilter();
    }

    private void applyFilter() {
        filteredTasks.clear();
        for (DownloadTask task : sourceTasks) {
            if (filterPredicate.test(task)) {
                filteredTasks.add(task);
            }
        }
    }

    @Override
    public boolean hasNext() {
        return currentIndex < filteredTasks.size();
    }

    @Override
    public DownloadTask next() {
        if (!hasNext()) {
            throw new NoSuchElementException("StatusFilteredTaskIterator has reached the end of matching tasks.");
        }
        DownloadTask task = filteredTasks.get(currentIndex++);
        System.out.printf("[StatusFilteredTaskIterator] [Stub] next() -> index=%d, file=\"%s\", status=%s%n",
                currentIndex - 1, task.getFileName(), task.getStatus());
        return task;
    }

    @Override
    public void reset() {
        System.out.println("[StatusFilteredTaskIterator] [Stub] reset() - Refreshing filter and rewinding position.");
        applyFilter();
        this.currentIndex = 0;
    }

    public int getMatchCount() {
        return filteredTasks.size();
    }
}
