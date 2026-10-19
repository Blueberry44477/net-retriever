package io.github.blueberry44477.net_retriever.patterns.structural.flyweight;

import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

/**
 * Context in the GoF Flyweight pattern storing extrinsic, non-shareable state.
 * Maintains a reference to the shared {@link ProtocolPolicyFlyweight} instance.
 */
@Getter
public class DownloadChunkContext {

    private final long chunkId;
    private final long byteOffset;
    private final int length;
    private final String destination;
    private final ProtocolPolicyFlyweight policy;

    @Setter
    private ChunkStatus status;

    public DownloadChunkContext(long chunkId, long byteOffset, int length, String destination, ProtocolPolicyFlyweight policy) {
        this.chunkId = chunkId;
        this.byteOffset = byteOffset;
        this.length = length;
        this.destination = Objects.requireNonNull(destination, "destination must not be null");
        this.policy = Objects.requireNonNull(policy, "policy must not be null");
        this.status = ChunkStatus.PENDING;
    }

    public void download() {
        this.status = ChunkStatus.DOWNLOADING;
        policy.transferChunk(chunkId, byteOffset, length, destination);
        this.status = ChunkStatus.COMPLETED;
    }
}
