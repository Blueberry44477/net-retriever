package io.github.blueberry44477.net_retriever.patterns.structural.flyweight;

/**
 * Status lifecycle of a download chunk in the extrinsic context.
 */
public enum ChunkStatus {
    PENDING,
    DOWNLOADING,
    COMPLETED,
    FAILED
}
