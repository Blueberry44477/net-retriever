package io.github.blueberry44477.net_retriever.patterns.behavioral.strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.Getter;

/**
 * Context in the GoF Strategy pattern.
 * Holds a reference to a {@link ChunkPartitioningStrategy} and coordinates payload partitioning.
 * Enables runtime strategy switching and enforces strict mathematical continuity invariants
 * on the partitioned byte ranges.
 */
@Getter
public class DownloadPartitionContext {

    private ChunkPartitioningStrategy strategy;

    public DownloadPartitionContext(ChunkPartitioningStrategy initialStrategy) {
        setStrategy(initialStrategy);
    }

    /**
     * Replaces the current partitioning strategy dynamically at runtime.
     *
     * @param strategy new partitioning strategy
     */
    public void setStrategy(ChunkPartitioningStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "strategy must not be null");
        System.out.printf("[DownloadPartitionContext] Active partitioning strategy configured: %s%n",
                this.strategy.getStrategyName());
    }

    /**
     * Executes the configured strategy to partition the payload and validates
     * all range continuity and boundary invariants.
     *
     * @param totalBytes total payload size in bytes
     * @param maxWorkers maximum worker count
     * @return validated unmodifiable list of chunk ranges
     */
    public List<DownloadChunkRange> executePartition(long totalBytes, int maxWorkers) {
        if (totalBytes <= 0) {
            throw new IllegalArgumentException("totalBytes must be greater than zero: " + totalBytes);
        }
        if (maxWorkers < 1) {
            throw new IllegalArgumentException("maxWorkers must be at least 1: " + maxWorkers);
        }

        List<DownloadChunkRange> chunks = strategy.partition(totalBytes, maxWorkers);
        validatePartitionInvariants(chunks, totalBytes);

        System.out.printf("[DownloadPartitionContext] [Strategy: %s] Partitioned %d bytes into %d chunk(s) (maxWorkers=%d)%n",
                strategy.getStrategyName(), totalBytes, chunks.size(), maxWorkers);

        return chunks;
    }

    /**
     * Generates analytical summary metrics for the given partition output.
     *
     * @param chunks partitioned chunks
     * @return summary metrics
     */
    public ChunkPartitionSummary getPartitionSummary(List<DownloadChunkRange> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            throw new IllegalArgumentException("chunks list must not be empty");
        }

        long totalBytes = 0;
        long minChunk = Long.MAX_VALUE;
        long maxChunk = Long.MIN_VALUE;
        Map<Integer, Long> workerDistribution = new HashMap<>();

        for (DownloadChunkRange chunk : chunks) {
            long len = chunk.length();
            totalBytes += len;
            minChunk = Math.min(minChunk, len);
            maxChunk = Math.max(maxChunk, len);

            workerDistribution.merge(chunk.assignedWorkerId(), len, Long::sum);
        }

        double avg = (double) totalBytes / chunks.size();

        long minWorker = Long.MAX_VALUE;
        long maxWorker = Long.MIN_VALUE;
        for (long workerBytes : workerDistribution.values()) {
            minWorker = Math.min(minWorker, workerBytes);
            maxWorker = Math.max(maxWorker, workerBytes);
        }

        double imbalanceRatio = minWorker > 0 ? (double) maxWorker / minWorker : 1.0;

        return new ChunkPartitionSummary(
                chunks.size(),
                totalBytes,
                minChunk,
                maxChunk,
                avg,
                workerDistribution,
                imbalanceRatio
        );
    }

    private void validatePartitionInvariants(List<DownloadChunkRange> chunks, long totalBytes) {
        if (chunks == null || chunks.isEmpty()) {
            throw new IllegalStateException("Strategy returned an empty or null chunk list");
        }

        long expectedStart = 0;
        long totalCovered = 0;

        for (int i = 0; i < chunks.size(); i++) {
            DownloadChunkRange current = chunks.get(i);
            if (current.startByte() != expectedStart) {
                throw new IllegalStateException(String.format(
                        "Range continuity gap detected at chunk %d: expected start %d, found %d",
                        i, expectedStart, current.startByte()
                ));
            }
            if (i > 0) {
                DownloadChunkRange previous = chunks.get(i - 1);
                if (previous.overlaps(current)) {
                    throw new IllegalStateException(String.format(
                            "Chunk overlap detected between chunk %d and chunk %d", i - 1, i
                    ));
                }
            }
            totalCovered += current.length();
            expectedStart = current.endByte() + 1;
        }

        if (expectedStart != totalBytes) {
            throw new IllegalStateException(String.format(
                    "Partition boundary violation: expected end offset %d, found %d",
                    totalBytes, expectedStart
            ));
        }

        if (totalCovered != totalBytes) {
            throw new IllegalStateException(String.format(
                    "Partition byte sum mismatch: expected %d bytes, got %d bytes",
                    totalBytes, totalCovered
            ));
        }
    }
}
