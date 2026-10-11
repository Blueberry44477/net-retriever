package io.github.blueberry44477.net_retriever.patterns.creational.factory_method;

/**
 * Factory Method pattern: Product interface for protocol-specific downloaders.
 */
public interface ProtocolDownloader {

    String getProtocolName();

    void downloadFile(String url, String destinationPath);

    boolean verifySource(String url);
}
