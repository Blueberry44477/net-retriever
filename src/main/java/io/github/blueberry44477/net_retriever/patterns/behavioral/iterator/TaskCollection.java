package io.github.blueberry44477.net_retriever.patterns.behavioral.iterator;

import java.util.Comparator;
import java.util.function.Predicate;

import io.github.blueberry44477.net_retriever.downloadtask.DownloadTask;
import io.github.blueberry44477.net_retriever.downloadtask.enums.DownloadStatus;

/**
 * Aggregate interface in the GoF Iterator pattern.
 * Defines the contract for an aggregate collection that produces specialized {@link TaskIterator} instances.
 */
public interface TaskCollection {

    /**
     * Appends a download task to the collection.
     *
     * @param task the download task to add
     */
    void addTask(DownloadTask task);

    /**
     * Removes a download task from the collection.
     *
     * @param task the download task to remove
     * @return true if the task was found and removed, false otherwise
     */
    boolean removeTask(DownloadTask task);

    /**
     * Returns total number of tasks in the collection.
     *
     * @return collection size
     */
    int size();

    /**
     * Checks if the collection contains no elements.
     *
     * @return true if empty, false otherwise
     */
    boolean isEmpty();

    /**
     * Creates a standard sequential iterator traversing tasks in insertion order.
     *
     * @return {@link TaskIterator} for sequential traversal
     */
    TaskIterator createIterator();

    /**
     * Creates a filtered iterator traversing only tasks matching the specified {@link DownloadStatus}.
     *
     * @param status status filter criteria
     * @return {@link TaskIterator} for status-filtered traversal
     */
    TaskIterator createStatusIterator(DownloadStatus status);

    /**
     * Creates a priority-ordered iterator traversing tasks sorted by descending file size.
     *
     * @return {@link TaskIterator} for priority traversal
     */
    TaskIterator createPriorityIterator();

    /**
     * Creates a custom sorted iterator using a caller-provided comparator.
     *
     * @param comparator ordering comparator
     * @return {@link TaskIterator} for custom ordered traversal
     */
    TaskIterator createSortedIterator(Comparator<DownloadTask> comparator);

    /**
     * Creates a custom filtered iterator matching an arbitrary predicate.
     *
     * @param predicate filter criteria
     * @return {@link TaskIterator} for predicate-filtered traversal
     */
    TaskIterator createFilteredIterator(Predicate<DownloadTask> predicate);
}
