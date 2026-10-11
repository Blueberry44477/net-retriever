package io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.products;

import io.github.blueberry44477.net_retriever.patterns.creational.abstract_factory.ProtocolEngine;

/**
 * Concrete Product A2: FTP network transfer engine.
 */
public class FtpProtocolEngine implements ProtocolEngine {

    @Override
    public void connect(String endpoint) {
        System.out.println("[FtpProtocolEngine] [Stub] connect(\"" + endpoint + "\") - Establishing FTP control connection on port 21.");
    }

    @Override
    public void transferData(String destinationPath, long bytesCount) {
        System.out.println("[FtpProtocolEngine] [Stub] transferData(dest=\"" + destinationPath
                + "\", bytes=" + bytesCount + ") - Opening PASV mode data channel and executing RETR.");
    }

    @Override
    public void disconnect() {
        System.out.println("[FtpProtocolEngine] [Stub] disconnect() - Sending QUIT command to FTP server.");
    }
}
