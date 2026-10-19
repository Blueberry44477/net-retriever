package io.github.blueberry44477.net_retriever.patterns.structural.flyweight;

/**
 * Flyweight interface in the GoF Flyweight pattern.
 * Declares the contract through which flyweight instances receive and operate on extrinsic state.
 */
public interface ProtocolPolicyFlyweight {

    void transferChunk(long chunkId, long offset, int length, String destination);

    String getPolicyKey();

    String getProtocol();

    String getHost();

    int getPort();

    int getBufferSize();

    String getAuthScheme();
}
