package io.github.blueberry44477.net_retriever.patterns.behavioral.strategy;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable metrics summarizing the characteristics and load distribution of a chunk partition.
 *
 * @param totalChunks            total number of chunks generated
 * @param totalBytes             sum of byte lengths across all chunks
 * @param minChunkSize           smallest chunk size in bytes
 * @param maxChunkSize           largest chunk size in bytes
 * @param averageChunkSize       arithmetic mean chunk size
 * @param workerByteDistribution aggregated bytes allocated to each worker identifier
 * @param loadImbalanceRatio     ratio between highest and lowest worker byte allocations (1.0 = perfect parity)
 */
public record ChunkPartitionSummary(
        int totalChunks,
        long totalBytes,
        long minChunkSize,
        long maxChunkSize,
        double averageChunkSize,
        Map<Integer, Long> workerByteDistribution,
        double loadImbalanceRatio
) {
    public ChunkPartitionSummary {
        workerByteDistribution = Collections.unmodifiableMap(workerByteDistribution);
    }
}
