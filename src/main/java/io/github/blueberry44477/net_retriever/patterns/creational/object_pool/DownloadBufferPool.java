package io.github.blueberry44477.net_retriever.patterns.creational.object_pool;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;

/**
 * Object Pool pattern: thread-safe pool managing reusable {@link DownloadBuffer} instances.
 */
public class DownloadBufferPool {

    @Getter
    private final int maxPoolSize;
    @Getter
    private final int bufferCapacity;
    private final BlockingQueue<DownloadBuffer> availableBuffers;
    private final Set<DownloadBuffer> inUseBuffers;
    private final AtomicInteger createdBuffersCounter;

    public DownloadBufferPool(int maxPoolSize, int bufferCapacity) {
        if (maxPoolSize <= 0 || bufferCapacity <= 0) {
            throw new IllegalArgumentException("maxPoolSize and bufferCapacity must be greater than 0");
        }
        this.maxPoolSize = maxPoolSize;
        this.bufferCapacity = bufferCapacity;
        this.availableBuffers = new LinkedBlockingQueue<>(maxPoolSize);
        this.inUseBuffers = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.createdBuffersCounter = new AtomicInteger(0);

        System.out.println("[DownloadBufferPool] Initialized pool: maxPoolSize=" + maxPoolSize
                + ", bufferCapacity=" + bufferCapacity + " bytes");
    }

    /**
     * Borrows a buffer from the pool, creating a new one if available and under capacity limit.
     */
    public DownloadBuffer acquireBuffer(long timeoutMillis) {
        System.out.println("[DownloadBufferPool] [Acquire] Requesting buffer (available: "
                + availableBuffers.size() + ", in-use: " + inUseBuffers.size()
                + ", total created: " + createdBuffersCounter.get() + ")");

        DownloadBuffer buffer = availableBuffers.poll();

        if (buffer == null && createdBuffersCounter.get() < maxPoolSize) {
            synchronized (this) {
                if (createdBuffersCounter.get() < maxPoolSize) {
                    int newId = createdBuffersCounter.incrementAndGet();
                    buffer = new DownloadBuffer(newId, bufferCapacity);
                }
            }
        }

        if (buffer == null) {
            try {
                System.out.println("[DownloadBufferPool] Pool exhausted, waiting for released buffer (max " + timeoutMillis + " ms)...");
                buffer = availableBuffers.poll(timeoutMillis, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for buffer", e);
            }
        }

        if (buffer == null) {
            throw new IllegalStateException("Failed to acquire buffer: pool exhausted (limit: " + maxPoolSize + ")");
        }

        inUseBuffers.add(buffer);
        System.out.println("[DownloadBufferPool] Dispatched buffer #" + buffer.getBufferId() + " to client.");
        return buffer;
    }

    public DownloadBuffer acquireBuffer() {
        return acquireBuffer(5000);
    }

    /**
     * Returns a used buffer to the pool after resetting its internal state.
     */
    public void releaseBuffer(DownloadBuffer buffer) {
        if (buffer == null) {
            return;
        }

        if (!inUseBuffers.remove(buffer)) {
            System.err.println("[DownloadBufferPool] Warning: attempted to release untracked buffer");
            return;
        }

        buffer.reset();
        boolean returned = availableBuffers.offer(buffer);

        System.out.println("[DownloadBufferPool] [Release] Buffer #" + buffer.getBufferId()
                + " returned to pool (success: " + returned + "). Available: "
                + availableBuffers.size() + ", in-use: " + inUseBuffers.size());
    }

    public int getAvailableCount() {
        return availableBuffers.size();
    }

    public int getInUseCount() {
        return inUseBuffers.size();
    }

    public int getCreatedCount() {
        return createdBuffersCounter.get();
    }
}
