package io.github.blueberry44477.net_retriever.patterns.structural.composite;

import java.util.Collections;
import java.util.List;

/**
 * Component interface in the GoF Composite pattern.
 * Defines the unified contract for both individual leaf files and composite task groups.
 */
public interface DownloadItem {

    String getName();

    long getTotalBytes();

    long getDownloadedBytes();

    default double getProgressPercentage() {
        long total = getTotalBytes();
        return (total > 0) ? ((double) getDownloadedBytes() / total) * 100.0 : 0.0;
    }

    void start();

    void pause();

    void cancel();

    void printHierarchy(String indent);

    default void add(DownloadItem item) {
        throw new UnsupportedOperationException("Leaf nodes do not support adding child items.");
    }

    default void remove(DownloadItem item) {
        throw new UnsupportedOperationException("Leaf nodes do not support removing child items.");
    }

    default List<DownloadItem> getChildren() {
        return Collections.emptyList();
    }
}
