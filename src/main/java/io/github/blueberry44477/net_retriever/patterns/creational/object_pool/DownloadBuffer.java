package io.github.blueberry44477.net_retriever.patterns.creational.object_pool;

import java.util.Arrays;

import lombok.Getter;
import lombok.ToString;

/**
 * Object Pool pattern: reusable memory buffer for streaming download chunks.
 */
@Getter
@ToString
public class DownloadBuffer {

    private final int bufferId;
    private final byte[] memory;
    private final int capacity;
    private int currentSize;

    public DownloadBuffer(int bufferId, int capacity) {
        this.bufferId = bufferId;
        this.capacity = capacity;
        this.memory = new byte[capacity];
        this.currentSize = 0;
        System.out.println("[DownloadBuffer #" + bufferId + "] Allocated byte array memory of " + capacity + " bytes.");
    }

    public void writeData(byte[] source, int offset, int length) {
        System.out.println("[DownloadBuffer #" + bufferId + "] [Stub] writeData: writing " + length + " bytes");
        if (currentSize + length > capacity) {
            throw new IndexOutOfBoundsException("Buffer #" + bufferId + " overflow: max " + capacity + ", requested " + (currentSize + length));
        }
        System.arraycopy(source, offset, this.memory, currentSize, length);
        this.currentSize += length;
    }

    /**
     * Resets the buffer state for reuse by the next client.
     */
    public void reset() {
        System.out.println("[DownloadBuffer #" + bufferId + "] [Object Pool Reset] Resetting buffer state (size: " + currentSize + " -> 0).");
        this.currentSize = 0;
        Arrays.fill(this.memory, (byte) 0);
    }
}
