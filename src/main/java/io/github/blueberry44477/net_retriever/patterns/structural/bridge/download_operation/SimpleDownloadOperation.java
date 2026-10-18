package io.github.blueberry44477.net_retriever.patterns.structural.bridge.download_operation;

import io.github.blueberry44477.net_retriever.patterns.structural.bridge.download_transport.DownloadTransport;

/**
 * RefinedAbstraction: Sequential single-stream download operation.
 */
public class SimpleDownloadOperation extends DownloadOperation {

    public SimpleDownloadOperation(DownloadTransport transport) {
        super(transport);
    }

    @Override
    public void execute(String sourceUrl, String destinationPath) {
        System.out.println("[SimpleDownloadOperation] [Stub] execute(url=\"" + sourceUrl
                + "\", dest=\"" + destinationPath + "\") - Using transport: " + transport.getProtocolName());
        transport.connect(sourceUrl);
        long totalSize = transport.fetchContentLength(sourceUrl);
        byte[] data = transport.readChunk(0, (int) Math.min(totalSize, 64 * 1024));
        System.out.println("[SimpleDownloadOperation] [Stub] Completed sequential download. Bytes transferred: " + data.length);
        transport.disconnect();
    }
}
