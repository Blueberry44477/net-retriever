package io.github.blueberry44477.net_retriever.patterns.behavioral.iterator;

import io.github.blueberry44477.net_retriever.downloadtask.DownloadTask;

/**
 * Iterator interface in the GoF Iterator pattern.
 * Provides sequential access to elements of an aggregate without exposing its underlying representation.
 */
public interface TaskIterator {

    /**
     * Checks if there are more elements in the traversal.
     *
     * @return true if the iterator has more elements, false otherwise
     */
    boolean hasNext();

    /**
     * Returns the next {@link DownloadTask} in the iteration sequence.
     *
     * @return next download task
     * @throws java.util.NoSuchElementException if iteration has no more elements
     */
    DownloadTask next();

    /**
     * Resets the iterator traversal pointer to the beginning.
     */
    void reset();
}
