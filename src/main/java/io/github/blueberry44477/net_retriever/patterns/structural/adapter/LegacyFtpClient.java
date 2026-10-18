package io.github.blueberry44477.net_retriever.patterns.structural.adapter;

import lombok.Getter;

/**
 * Legacy third-party FTP client representing an incompatible external library.
 * Acts as an Adaptee in the GoF Object Adapter pattern.
 */
@Getter
public class LegacyFtpClient {

    private String connectedHost;
    private int connectedPort;
    private boolean loggedIn;

    public boolean connect(String host, int port, String user) {
        System.out.println("[LegacyFtpClient] [Stub] connect(host=\"" + host + "\", port=" + port
                + ", user=\"" + user + "\") - Establishing legacy FTP TCP connection.");
        this.connectedHost = host;
        this.connectedPort = port;
        this.loggedIn = true;
        return true;
    }

    public long getRemoteFileSize(String remoteFilePath) {
        System.out.println("[LegacyFtpClient] [Stub] getRemoteFileSize(remoteFilePath=\""
                + remoteFilePath + "\") - Issuing FTP SIZE command.");
        return 15_728_640L;
    }

    public boolean isRestartSupported() {
        System.out.println("[LegacyFtpClient] [Stub] isRestartSupported() - Checking REST 100 feature.");
        return true;
    }

    public byte[] retrieveStream(String remoteFilePath, long markerOffset, int bufferSize) {
        System.out.println("[LegacyFtpClient] [Stub] retrieveStream(remoteFilePath=\""
                + remoteFilePath + "\", markerOffset=" + markerOffset + ", bufferSize=" + bufferSize
                + ") - Transferring binary stream via passive data port.");
        return new byte[Math.min(bufferSize, 1024)];
    }

    public void disconnect() {
        System.out.println("[LegacyFtpClient] [Stub] disconnect() - Sending QUIT command to FTP server.");
        this.connectedHost = null;
        this.connectedPort = 0;
        this.loggedIn = false;
    }

}
