package io.github.blueberry44477.net_retriever.patterns.behavioral.strategy;

import java.util.Objects;

/**
 * Immutable value object representing a contiguous byte range chunk allocated for download.
 *
 * @param chunkIndex       zero-based sequence index of the chunk
 * @param startByte        inclusive starting byte offset
 * @param endByte          inclusive ending byte offset
 * @param length           total number of bytes in this chunk range
 * @param assignedWorkerId identifier of the worker thread or connection assigned to this chunk
 */
public record DownloadChunkRange(
        int chunkIndex,
        long startByte,
        long endByte,
        long length,
        int assignedWorkerId
) {
    public DownloadChunkRange {
        if (chunkIndex < 0) {
            throw new IllegalArgumentException("chunkIndex must be non-negative: " + chunkIndex);
        }
        if (startByte < 0) {
            throw new IllegalArgumentException("startByte must be non-negative: " + startByte);
        }
        if (endByte < startByte) {
            throw new IllegalArgumentException(String.format(
                    "endByte (%d) cannot be less than startByte (%d)", endByte, startByte
            ));
        }
        long computedLength = (endByte - startByte) + 1;
        if (length != computedLength) {
            throw new IllegalArgumentException(String.format(
                    "Explicit length (%d) does not match computed length (%d)", length, computedLength
            ));
        }
        if (assignedWorkerId < 0) {
            throw new IllegalArgumentException("assignedWorkerId must be non-negative: " + assignedWorkerId);
        }
    }

    public static DownloadChunkRange of(int chunkIndex, long startByte, long endByte, int assignedWorkerId) {
        long len = (endByte - startByte) + 1;
        return new DownloadChunkRange(chunkIndex, startByte, endByte, len, assignedWorkerId);
    }

    /**
     * Checks if a specific byte offset falls within this chunk range.
     *
     * @param offset byte offset to test
     * @return true if offset is in [startByte, endByte]
     */
    public boolean contains(long offset) {
        return offset >= startByte && offset <= endByte;
    }

    /**
     * Determines whether this chunk overlaps with another chunk range.
     *
     * @param other another chunk range
     * @return true if ranges overlap
     */
    public boolean overlaps(DownloadChunkRange other) {
        Objects.requireNonNull(other, "other chunk range must not be null");
        return this.startByte <= other.endByte && other.startByte <= this.endByte;
    }

    /**
     * Checks if this chunk is contiguous and directly adjacent to another chunk range.
     *
     * @param other another chunk range
     * @return true if adjacent
     */
    public boolean isAdjacent(DownloadChunkRange other) {
        Objects.requireNonNull(other, "other chunk range must not be null");
        return this.endByte + 1 == other.startByte || other.endByte + 1 == this.startByte;
    }

    /**
     * Formats this range into a standard RFC 9110 HTTP Range request header value.
     *
     * @return HTTP Range header value, e.g. "bytes=0-1048575"
     */
    public String toHttpRangeHeader() {
        return "bytes=" + startByte + "-" + endByte;
    }
}
