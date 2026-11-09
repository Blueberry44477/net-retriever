package io.github.blueberry44477.net_retriever.patterns.behavioral.strategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Strategy: Partitions payload into equal parts corresponding to the worker count.
 * Remainder bytes are evenly distributed across the leading partitions, ensuring that
 * the maximum byte differential between any two workers never exceeds 1 byte.
 */
public class EqualWorkerChunkingStrategy implements ChunkPartitioningStrategy {

    @Override
    public List<DownloadChunkRange> partition(long totalBytes, int maxWorkers) {
        if (totalBytes <= 0) {
            throw new IllegalArgumentException("totalBytes must be greater than zero: " + totalBytes);
        }
        if (maxWorkers < 1) {
            throw new IllegalArgumentException("maxWorkers must be at least 1: " + maxWorkers);
        }

        int effectiveWorkers = (int) Math.min((long) maxWorkers, totalBytes);
        long baseChunkSize = totalBytes / effectiveWorkers;
        long remainder = totalBytes % effectiveWorkers;

        List<DownloadChunkRange> chunks = new ArrayList<>(effectiveWorkers);
        long currentOffset = 0;

        for (int i = 0; i < effectiveWorkers; i++) {
            long currentChunkLen = baseChunkSize + (i < remainder ? 1 : 0);
            long endByte = currentOffset + currentChunkLen - 1;

            chunks.add(DownloadChunkRange.of(i, currentOffset, endByte, i));
            currentOffset += currentChunkLen;
        }

        return List.copyOf(chunks);
    }

    @Override
    public String getStrategyName() {
        return "EqualWorkerChunkingStrategy";
    }

    @Override
    public String getDescription() {
        return "Divides payload into balanced segments across available workers with 1-byte remainder balancing";
    }
}
