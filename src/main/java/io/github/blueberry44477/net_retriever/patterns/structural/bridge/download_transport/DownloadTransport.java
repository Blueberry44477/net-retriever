package io.github.blueberry44477.net_retriever.patterns.structural.bridge.download_transport;

/**
 * Implementor interface in the GoF Bridge pattern.
 * Defines the low-level primitive transport operations decoupled from high-level download workflows.
 */
public interface DownloadTransport {

    void connect(String sourceUrl);

    long fetchContentLength(String sourceUrl);

    boolean supportsRangeRequests();

    byte[] readChunk(long offset, int length);

    void disconnect();

    String getProtocolName();
}
