package io.github.blueberry44477.net_retriever.patterns.behavioral.iterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import io.github.blueberry44477.net_retriever.downloadtask.DownloadTask;
import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;

/**
 * Concrete Aggregate in the GoF Iterator pattern.
 * Manages an internal collection of {@link DownloadTask} instances and provides factory methods
 * to instantiate specialized traversal iterators without exposing internal storage.
 */
public class DownloadTaskCollection implements TaskCollection {

    private final List<DownloadTask> tasks = new ArrayList<>();

    public DownloadTaskCollection() {
    }

    public DownloadTaskCollection(List<DownloadTask> initialTasks) {
        if (initialTasks != null) {
            this.tasks.addAll(initialTasks);
        }
    }

    @Override
    public void addTask(DownloadTask task) {
        Objects.requireNonNull(task, "DownloadTask must not be null");
        System.out.printf("[DownloadTaskCollection] [Stub] addTask(file=\"%s\", status=%s)%n",
                task.getFileName(), task.getStatus());
        tasks.add(task);
    }

    @Override
    public boolean removeTask(DownloadTask task) {
        Objects.requireNonNull(task, "DownloadTask must not be null");
        System.out.printf("[DownloadTaskCollection] [Stub] removeTask(file=\"%s\")%n", task.getFileName());
        return tasks.remove(task);
    }

    @Override
    public int size() {
        return tasks.size();
    }

    @Override
    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    @Override
    public TaskIterator createIterator() {
        System.out.printf("[DownloadTaskCollection] [Stub] createIterator() - Spawning SequentialTaskIterator for %d tasks%n",
                tasks.size());
        return new SequentialTaskIterator(tasks);
    }

    @Override
    public TaskIterator createStatusIterator(DownloadStatus status) {
        System.out.printf("[DownloadTaskCollection] [Stub] createStatusIterator(status=%s) - Spawning StatusFilteredTaskIterator%n",
                status);
        return new StatusFilteredTaskIterator(tasks, status);
    }

    @Override
    public TaskIterator createPriorityIterator() {
        System.out.printf("[DownloadTaskCollection] [Stub] createPriorityIterator() - Spawning PriorityTaskIterator for %d tasks%n",
                tasks.size());
        return new PriorityTaskIterator(tasks);
    }

    @Override
    public TaskIterator createSortedIterator(Comparator<DownloadTask> comparator) {
        System.out.println("[DownloadTaskCollection] [Stub] createSortedIterator() - Spawning custom PriorityTaskIterator");
        return new PriorityTaskIterator(tasks, comparator);
    }

    @Override
    public TaskIterator createFilteredIterator(Predicate<DownloadTask> predicate) {
        System.out.println("[DownloadTaskCollection] [Stub] createFilteredIterator() - Spawning custom StatusFilteredTaskIterator");
        return new StatusFilteredTaskIterator(tasks, predicate);
    }

    /**
     * Returns an unmodifiable snapshot view of underlying tasks.
     *
     * @return unmodifiable list of tasks
     */
    public List<DownloadTask> getTasksSnapshot() {
        return Collections.unmodifiableList(new ArrayList<>(tasks));
    }

    /**
     * Removes all elements from the collection.
     */
    public void clear() {
        tasks.clear();
    }
}
