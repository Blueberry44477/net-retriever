package io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.products;

import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.ProtocolEngine;

/**
 * Concrete Product A1: HTTP/HTTPS network transfer engine.
 */
public class HttpProtocolEngine implements ProtocolEngine {

    @Override
    public void connect(String endpoint) {
        System.out.println("[HttpProtocolEngine] [Stub] connect(\"" + endpoint + "\") - Establishing TLS/HTTP connection over TCP 443/80.");
    }

    @Override
    public void transferData(String destinationPath, long bytesCount) {
        System.out.println("[HttpProtocolEngine] [Stub] transferData(dest=\"" + destinationPath
                + "\", bytes=" + bytesCount + ") - Streaming response body into local file.");
    }

    @Override
    public void disconnect() {
        System.out.println("[HttpProtocolEngine] [Stub] disconnect() - Closing persistent HTTP keep-alive connection.");
    }
}
