package io.github.blueberry44477.net_retriever.patterns.structural.bridge.download_transport;

/**
 * ConcreteImplementor for Cloud Object Storage (S3 / GCS / Azure Blob) transport.
 */
public class CloudStorageTransport implements DownloadTransport {

    private boolean connected;

    @Override
    public void connect(String sourceUrl) {
        System.out.println("[CloudStorageTransport] [Stub] connect(url=\"" + sourceUrl
                + "\") - Authenticating via IAM credentials against cloud endpoint.");
        this.connected = true;
    }

    @Override
    public long fetchContentLength(String sourceUrl) {
        System.out.println("[CloudStorageTransport] [Stub] fetchContentLength(url=\"" + sourceUrl
                + "\") - Invoking HeadObject on target bucket.");
        return 52_428_800L;
    }

    @Override
    public boolean supportsRangeRequests() {
        System.out.println("[CloudStorageTransport] [Stub] supportsRangeRequests() - Verifying blob byte-range indexing.");
        return true;
    }

    @Override
    public byte[] readChunk(long offset, int length) {
        System.out.println("[CloudStorageTransport] [Stub] readChunk(offset=" + offset + ", length="
                + length + ") - Downloading byte range part with AWS SigV4 authorization.");
        return new byte[Math.min(length, 1024)];
    }

    @Override
    public void disconnect() {
        System.out.println("[CloudStorageTransport] [Stub] disconnect() - Releasing cloud client resources.");
        this.connected = false;
    }

    @Override
    public String getProtocolName() {
        return "CloudStorage/S3";
    }

    public boolean isConnected() {
        return connected;
    }
}
