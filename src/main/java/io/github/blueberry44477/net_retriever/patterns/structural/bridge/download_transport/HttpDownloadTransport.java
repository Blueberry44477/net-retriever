package io.github.blueberry44477.net_retriever.patterns.structural.bridge.download_transport;

/**
 * ConcreteImplementor for HTTP/HTTPS transport.
 */
public class HttpDownloadTransport implements DownloadTransport {

    private boolean connected;

    @Override
    public void connect(String sourceUrl) {
        System.out.println("[HttpDownloadTransport] [Stub] connect(url=\"" + sourceUrl
                + "\") - Establishing HTTP/2 TLS session.");
        this.connected = true;
    }

    @Override
    public long fetchContentLength(String sourceUrl) {
        System.out.println("[HttpDownloadTransport] [Stub] fetchContentLength(url=\"" + sourceUrl
                + "\") - Sending HTTP HEAD request.");
        return 20_971_520L;
    }

    @Override
    public boolean supportsRangeRequests() {
        System.out.println("[HttpDownloadTransport] [Stub] supportsRangeRequests() - Evaluating Accept-Ranges header.");
        return true;
    }

    @Override
    public byte[] readChunk(long offset, int length) {
        System.out.println("[HttpDownloadTransport] [Stub] readChunk(offset=" + offset + ", length="
                + length + ") - HTTP GET with Range: bytes=" + offset + "-" + (offset + length - 1));
        return new byte[Math.min(length, 1024)];
    }

    @Override
    public void disconnect() {
        System.out.println("[HttpDownloadTransport] [Stub] disconnect() - Returning HTTP connection to pool.");
        this.connected = false;
    }

    @Override
    public String getProtocolName() {
        return "HTTP/HTTPS";
    }

    public boolean isConnected() {
        return connected;
    }
}
