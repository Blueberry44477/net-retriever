package io.github.blueberry44477.net_retriever.patterns.behavioral.strategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Strategy: Dynamically adapts chunk counts and sizing based on file size tiers.
 * - Files under 5 MB: single chunk (avoids socket overhead and connection handshakes).
 * - Files 5 MB - 50 MB: up to 4 parallel segments.
 * - Files 50 MB - 500 MB: up to 8 parallel segments.
 * - Files > 500 MB: up to 16 parallel segments (or maxWorkers).
 */
public class AdaptiveDynamicChunkingStrategy implements ChunkPartitioningStrategy {

    public static final long TIER_SMALL_MAX_BYTES = 5L * 1024 * 1024;     // 5 MB
    public static final long TIER_MEDIUM_MAX_BYTES = 50L * 1024 * 1024;   // 50 MB
    public static final long TIER_LARGE_MAX_BYTES = 500L * 1024 * 1024;   // 500 MB

    @Override
    public List<DownloadChunkRange> partition(long totalBytes, int maxWorkers) {
        if (totalBytes <= 0) {
            throw new IllegalArgumentException("totalBytes must be greater than zero: " + totalBytes);
        }
        if (maxWorkers < 1) {
            throw new IllegalArgumentException("maxWorkers must be at least 1: " + maxWorkers);
        }

        int targetChunks;
        if (totalBytes < TIER_SMALL_MAX_BYTES) {
            targetChunks = 1;
        } else if (totalBytes < TIER_MEDIUM_MAX_BYTES) {
            targetChunks = Math.min(4, maxWorkers);
        } else if (totalBytes < TIER_LARGE_MAX_BYTES) {
            targetChunks = Math.min(8, maxWorkers);
        } else {
            targetChunks = Math.min(16, maxWorkers);
        }

        // Clamp to valid bounds
        targetChunks = (int) Math.max(1, Math.min((long) targetChunks, totalBytes));

        long baseChunkSize = totalBytes / targetChunks;
        long remainder = totalBytes % targetChunks;

        List<DownloadChunkRange> chunks = new ArrayList<>(targetChunks);
        long currentOffset = 0;

        for (int i = 0; i < targetChunks; i++) {
            long currentChunkLen = baseChunkSize + (i < remainder ? 1 : 0);
            long endByte = currentOffset + currentChunkLen - 1;
            int assignedWorker = i % maxWorkers;

            chunks.add(DownloadChunkRange.of(i, currentOffset, endByte, assignedWorker));
            currentOffset += currentChunkLen;
        }

        return List.copyOf(chunks);
    }

    @Override
    public String getStrategyName() {
        return "AdaptiveDynamicChunkingStrategy";
    }

    @Override
    public String getDescription() {
        return "Calculates chunk count adaptively based on file size tiers (1, 4, 8, or 16 segments)";
    }
}
