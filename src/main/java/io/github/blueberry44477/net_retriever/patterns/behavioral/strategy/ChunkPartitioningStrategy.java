package io.github.blueberry44477.net_retriever.patterns.behavioral.strategy;

import java.util.List;

/**
 * Strategy interface in the GoF Strategy pattern.
 * Defines the contract for interchangeable file byte-range partitioning algorithms.
 * Encapsulates how a payload of totalBytes is split across worker threads or mirror connections.
 */
public interface ChunkPartitioningStrategy {

    /**
     * Partitions the given payload byte size into an ordered list of non-overlapping,
     * fully contiguous {@link DownloadChunkRange} instances.
     *
     * @param totalBytes total payload size in bytes (must be > 0)
     * @param maxWorkers maximum number of parallel worker threads / connections available (must be >= 1)
     * @return contiguous, non-overlapping list of chunk ranges covering [0, totalBytes - 1]
     * @throws IllegalArgumentException if {@code totalBytes <= 0} or {@code maxWorkers < 1}
     */
    List<DownloadChunkRange> partition(long totalBytes, int maxWorkers);

    /**
     * Returns the human-readable identifier of the partitioning strategy.
     *
     * @return strategy name
     */
    String getStrategyName();

    /**
     * Returns a brief description of the partitioning heuristic or algorithmic approach.
     *
     * @return strategy description
     */
    String getDescription();
}
