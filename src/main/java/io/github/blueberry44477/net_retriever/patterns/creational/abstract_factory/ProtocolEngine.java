package io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory;

/**
 * Abstract Factory: Abstract Product A (network protocol engine).
 */
public interface ProtocolEngine {

    void connect(String endpoint);

    void transferData(String destinationPath, long bytesCount);

    void disconnect();
}
