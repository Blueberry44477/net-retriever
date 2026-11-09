package io.github.blueberry44477.net_retriever.patterns.behavioral.strategy;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * Concrete Strategy: Partitions the payload into uniform, fixed-size chunks.
 * Useful for predictable buffer management, caching proxies, and disk page alignment.
 * Workers are assigned in round-robin sequence across chunks.
 */
@Getter
public class FixedSizeChunkingStrategy implements ChunkPartitioningStrategy {

    public static final long DEFAULT_CHUNK_SIZE_BYTES = 2L * 1024 * 1024; // 2 MB

    private final long chunkSizeBytes;

    public FixedSizeChunkingStrategy() {
        this(DEFAULT_CHUNK_SIZE_BYTES);
    }

    public FixedSizeChunkingStrategy(long chunkSizeBytes) {
        if (chunkSizeBytes <= 0) {
            throw new IllegalArgumentException("chunkSizeBytes must be greater than zero: " + chunkSizeBytes);
        }
        this.chunkSizeBytes = chunkSizeBytes;
    }

    @Override
    public List<DownloadChunkRange> partition(long totalBytes, int maxWorkers) {
        if (totalBytes <= 0) {
            throw new IllegalArgumentException("totalBytes must be greater than zero: " + totalBytes);
        }
        if (maxWorkers < 1) {
            throw new IllegalArgumentException("maxWorkers must be at least 1: " + maxWorkers);
        }

        List<DownloadChunkRange> chunks = new ArrayList<>();
        long currentOffset = 0;
        int chunkIndex = 0;

        while (currentOffset < totalBytes) {
            long remaining = totalBytes - currentOffset;
            long currentChunkLen = Math.min(chunkSizeBytes, remaining);
            long endByte = currentOffset + currentChunkLen - 1;
            int assignedWorker = chunkIndex % maxWorkers;

            chunks.add(DownloadChunkRange.of(chunkIndex, currentOffset, endByte, assignedWorker));
            currentOffset += currentChunkLen;
            chunkIndex++;
        }

        return List.copyOf(chunks);
    }

    @Override
    public String getStrategyName() {
        return "FixedSizeChunkingStrategy";
    }

    @Override
    public String getDescription() {
        return String.format("Partitions payload into uniform %d-byte fixed blocks with round-robin worker assignment",
                chunkSizeBytes);
    }
}
