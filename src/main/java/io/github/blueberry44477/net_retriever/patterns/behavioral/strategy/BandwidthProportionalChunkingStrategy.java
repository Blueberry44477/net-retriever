package io.github.blueberry44477.net_retriever.patterns.behavioral.strategy;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * Concrete Strategy: Partitions payload proportionally to heterogeneous worker bandwidth weights.
 * Addresses the straggler problem in distributed downloads by giving higher-capacity
 * connections or faster mirrors larger byte portions so that all connections complete simultaneously.
 */
@Getter
public class BandwidthProportionalChunkingStrategy implements ChunkPartitioningStrategy {

    private final List<Double> workerWeights;

    public BandwidthProportionalChunkingStrategy(List<Double> workerWeights) {
        if (workerWeights == null || workerWeights.isEmpty()) {
            throw new IllegalArgumentException("workerWeights must not be null or empty");
        }
        for (Double weight : workerWeights) {
            if (weight == null || weight <= 0.0) {
                throw new IllegalArgumentException("All worker weights must be strictly positive: " + weight);
            }
        }
        this.workerWeights = List.copyOf(workerWeights);
    }

    @Override
    public List<DownloadChunkRange> partition(long totalBytes, int maxWorkers) {
        if (totalBytes <= 0) {
            throw new IllegalArgumentException("totalBytes must be greater than zero: " + totalBytes);
        }
        if (maxWorkers < 1) {
            throw new IllegalArgumentException("maxWorkers must be at least 1: " + maxWorkers);
        }

        int activeWorkers = (int) Math.min((long) Math.min(workerWeights.size(), maxWorkers), totalBytes);

        double totalWeight = 0.0;
        for (int i = 0; i < activeWorkers; i++) {
            totalWeight += workerWeights.get(i);
        }

        List<DownloadChunkRange> chunks = new ArrayList<>(activeWorkers);
        long currentOffset = 0;

        for (int i = 0; i < activeWorkers; i++) {
            long currentChunkLen;
            if (i == activeWorkers - 1) {
                // Last worker gets all remaining bytes to eliminate rounding errors
                currentChunkLen = totalBytes - currentOffset;
            } else {
                double fraction = workerWeights.get(i) / totalWeight;
                long rawLen = Math.round(totalBytes * fraction);
                // Ensure at least 1 byte, and leave at least (activeWorkers - 1 - i) bytes for remaining workers
                long remainingWorkersCount = activeWorkers - 1 - i;
                long maxAllowedForThisWorker = (totalBytes - currentOffset) - remainingWorkersCount;
                currentChunkLen = Math.max(1, Math.min(rawLen, maxAllowedForThisWorker));
            }

            long endByte = currentOffset + currentChunkLen - 1;
            chunks.add(DownloadChunkRange.of(i, currentOffset, endByte, i));
            currentOffset += currentChunkLen;
        }

        return List.copyOf(chunks);
    }

    @Override
    public String getStrategyName() {
        return "BandwidthProportionalChunkingStrategy";
    }

    @Override
    public String getDescription() {
        return "Distributes chunk byte ranges proportionally according to worker bandwidth capacities: "
                + workerWeights;
    }
}
