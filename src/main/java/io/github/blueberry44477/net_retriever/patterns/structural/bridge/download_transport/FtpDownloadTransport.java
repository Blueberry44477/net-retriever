package io.github.blueberry44477.net_retriever.patterns.structural.bridge.download_transport;

/**
 * ConcreteImplementor for FTP transport.
 */
public class FtpDownloadTransport implements DownloadTransport {

    private boolean connected;

    @Override
    public void connect(String sourceUrl) {
        System.out.println("[FtpDownloadTransport] [Stub] connect(url=\"" + sourceUrl
                + "\") - Opening FTP control channel on port 21.");
        this.connected = true;
    }

    @Override
    public long fetchContentLength(String sourceUrl) {
        System.out.println("[FtpDownloadTransport] [Stub] fetchContentLength(url=\"" + sourceUrl
                + "\") - Executing FTP SIZE command.");
        return 10_485_760L;
    }

    @Override
    public boolean supportsRangeRequests() {
        System.out.println("[FtpDownloadTransport] [Stub] supportsRangeRequests() - Checking REST command support.");
        return true;
    }

    @Override
    public byte[] readChunk(long offset, int length) {
        System.out.println("[FtpDownloadTransport] [Stub] readChunk(offset=" + offset + ", length="
                + length + ") - Setting REST marker and reading passive stream.");
        return new byte[Math.min(length, 1024)];
    }

    @Override
    public void disconnect() {
        System.out.println("[FtpDownloadTransport] [Stub] disconnect() - Sending QUIT and terminating connection.");
        this.connected = false;
    }

    @Override
    public String getProtocolName() {
        return "FTP";
    }

    public boolean isConnected() {
        return connected;
    }
}
